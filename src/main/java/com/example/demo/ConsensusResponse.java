package com.example.demo;

import com.example.demo.blockchain.Chain;

public class ConsensusResponse {
    protected String message;
    protected Chain chain;

    public ConsensusResponse(String message, Chain chain) {
        this.message = message;
        this.chain = chain;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Chain getChain() {
        return chain;
    }

    public void setChain(Chain chain) {
        this.chain = chain;
    }
}
