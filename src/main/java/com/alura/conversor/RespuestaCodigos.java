package com.alura.conversor;

import com.google.gson.annotations.SerializedName;

public record RespuestaCodigos(@SerializedName("supported_codes") String[][] supportedCodes) {
}
