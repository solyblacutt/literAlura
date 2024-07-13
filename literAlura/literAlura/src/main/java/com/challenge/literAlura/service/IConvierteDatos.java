package com.challenge.literAlura.service;

public interface IConvierteDatos {
    <T> T convierteDatos(String json, Class<T> clase);
}
