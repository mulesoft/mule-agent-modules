package com.mulesoft.agent.common.builders;

public interface MessageBuilder<TFinal>
{
    TFinal build (Object message);
}
