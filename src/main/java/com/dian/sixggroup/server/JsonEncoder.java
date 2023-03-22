package com.dian.sixggroup.server;

import com.dian.sixggroup.common.Packet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * @Author: tanjun
 * @CreateTime: 2023-03-07 15:34
 */
public class JsonEncoder implements Encoder.Text<Packet> {

    private static Logger logger = LoggerFactory.getLogger(JsonEncoder.class);

    @Override
    public String encode(Packet packet) throws EncodeException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = "";
        try {
            json = objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            logger.error("json parse error", e);
        }
        return json;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
