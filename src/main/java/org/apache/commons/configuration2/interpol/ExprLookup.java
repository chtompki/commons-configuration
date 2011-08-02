/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration2.interpol;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.ConfigurationRuntimeException;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;

import java.util.ArrayList;

/**
 * Lookup that allows expressions to be evaluated.
 *
 * <pre>
 *     ExprLookup.Variables vars = new ExprLookup.Variables();
 *     vars.add(new ExprLookup.Variable("String", org.apache.commons.lang3.StringUtils.class));
 *     vars.add(new ExprLookup.Variable("Util", new Utility("Hello")));
 *     vars.add(new ExprLookup.Variable("System", "Class:java.lang.System"));
 *     XMLConfiguration config = new XMLConfiguration(TEST_FILE);
 *     config.setLogger(log);
 *     ExprLookup lookup = new ExprLookup(vars);
 *     lookup.setConfiguration(config);
 *     String str = lookup.lookup("'$[element] ' + String.trimToEmpty('$[space.description]')");
 * </pre>
 *
 * In the example above TEST_FILE contains xml that looks like:
 * <pre>
 * &lt;configuration&gt;
 *   &lt;element&gt;value&lt;/element&gt;
 *   &lt;space xml:space="preserve"&gt;
 *     &lt;description xml:space="default"&gt;     Some text      &lt;/description&gt;
 *   &lt;/space&gt;
 * &lt;/configuration&gt;
 * </pre>
 *
 * The result will be "value Some text".
 *
 * This lookup uses Apache Commons Jexl and requires that the dependency be added to any
 * projects which use this.
 *
 * @since 1.7
 * @author <a
 * href="http://commons.apache.org/configuration/team-list.html">Commons Configuration team</a>
 * @version $Id$
 */
public class ExprLookup extends StrLookup
{
    /** Prefix to identify a Java Class object */
    private static final String CLASS = "Class:";

    /** The default prefix for subordinate lookup expressions */
    private static final String DEFAULT_PREFIX = "$[";

    /** The default suffix for subordinate lookup expressions */
    private static final String DEFAULT_SUFFIX = "]";

    /** Configuration being operated on */
    private AbstractConfiguration configuration;

    /** The JexlContext */
    private JexlContext context = JexlHelper.createContext();

    /** The String to use to start subordinate lookup expressions */
    private String prefixMatcher = DEFAULT_PREFIX;

    /** The String to use to terminate subordinate lookup expressions */
    private String suffixMatcher = DEFAULT_SUFFIX;

    /**
     * The default constructor. Will get used when the Lookup is constructed via
     * configuration.
     */
    public ExprLookup()
    {
    }

    /**
     * Constructor for use by applications.
     * @param list The list of objects to be accessible in expressions.
     */
    public ExprLookup(Variables list)
    {
        setVariables(list);
    }

    /**
     * Constructor for use by applications.
     * @param list The list of objects to be accessible in expressions.
     * @param prefix The prefix to use for subordinate lookups.
     * @param suffix The suffix to use for subordinate lookups.
     */
    public ExprLookup(Variables list, String prefix, String suffix)
    {
        this(list);
        setVariablePrefixMatcher(prefix);
        setVariableSuffixMatcher(suffix);
    }

    /**
     * Set the prefix to use to identify subordinate expressions. This cannot be the
     * same as the prefix used for the primary expression.
     * @param prefix The String identifying the beginning of the expression.
     */
    public void setVariablePrefixMatcher(String prefix)
    {
        prefixMatcher = prefix;
    }


    /**
     * Set the suffix to use to identify subordinate expressions. This cannot be the
     * same as the suffix used for the primary expression.
     * @param suffix The String identifying the end of the expression.
     */
    public void setVariableSuffixMatcher(String suffix)
    {
        suffixMatcher = suffix;
    }

    /**
     * Add the Variables that will be accessible within expressions.
     * @param list The list of Variables.
     */
    public void setVariables(Variables list)
    {
        for (Variable var : list)
        {
            context.getVars().put(var.getName(), var.getValue());
        }
    }

    /**
     * Returns the list of Variables that are accessible within expressions.
     * @return the List of Variables that are accessible within expressions.
     */
    public Variables getVariables()
    {
        return null;
    }

    /**
     * Set the configuration to be used to interpolate subordinate expressiosn.
     * @param config The Configuration.
     */
    public void setConfiguration(AbstractConfiguration config)
    {
        this.configuration = config;
    }

    /**
     * Evaluates the expression.
     * @param var The expression.
     * @return The String result of the expression.
     */
    public String lookup(String var)
    {
        ConfigurationInterpolator interp = configuration.getInterpolator();
        StrSubstitutor subst = new StrSubstitutor(interp, prefixMatcher, suffixMatcher,
                StrSubstitutor.DEFAULT_ESCAPE);

        String result = subst.replace(var);

        try
        {
            Expression exp = ExpressionFactory.createExpression(result);
            result = (String) exp.evaluate(context);
        }
        catch (Exception e)
        {
            configuration.getLogger().debug( "Error encountered evaluating " + result, e);
        }

        return result;
    }

    /**
     * List wrapper used to allow the Variables list to be created as beans in
     * DefaultConfigurationBuilder.
     */
    public static class Variables extends ArrayList<Variable>
    {
        /*
        public void setVariable(Variable var)
        {
            add(var);
        } */

        public Variable getVariable()
        {
            if (size() > 0)
            {
                return get(size() - 1);
            }
            else
            {
                return null;
            }
        }

    }

    /**
     * The key and corresponding object that will be made available to the
     * JexlContext for use in expressions.
     */
    public static class Variable
    {
        /** The name to be used in expressions */
        private String key;

        /** The object to be accessed in expressions */
        private Object value;

        public Variable()
        {
        }

        public Variable(String name, Object value)
        {
            setName(name);
            setValue(value);
        }

        public String getName()
        {
            return key;
        }

        public void setName(String name)
        {
            this.key = name;
        }

        public Object getValue()
        {
            return value;
        }

        public void setValue(Object value) throws ConfigurationRuntimeException
        {
            try
            {
                if (!(value instanceof String))
                {
                    this.value = value;
                    return;
                }
                String val = (String) value;
                String name = StringUtils.removeStartIgnoreCase(val, CLASS);
                Class clazz = ClassUtils.getClass(name);
                if (name.length() == val.length())
                {
                    this.value = clazz.newInstance();
                }
                else
                {
                    this.value = clazz;
                }
            }
            catch (Exception e)
            {
                throw new ConfigurationRuntimeException("Unable to create " + value, e);
            }

        }
    }
}