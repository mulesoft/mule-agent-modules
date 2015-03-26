package com.mulesoft.agent.eventtracking;

import com.github.mustachejava.TemplateFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatterTemplateFunction implements TemplateFunction
{
    private final static Logger LOGGER = LoggerFactory.getLogger(DateFormatterTemplateFunction.class);
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public String apply (String input)
    {
        try
        {
            Date date = new Date(Long.parseLong(input));
            return dateFormat.format(date);
        }
        catch(Exception e)
        {
            LOGGER.error(String.format("The string '%s' couldn't be parsed as a date.", e));
            return null;
        }
    }
}
