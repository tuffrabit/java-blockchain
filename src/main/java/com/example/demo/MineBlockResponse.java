package com.example.demo;

import com.example.demo.blockchain.Transaction;

import java.util.List;

public class MineBlockResponse {
    protected String message;
    protected long index;
    protected List<Transaction> transactions;
    protected long proof;
    protected String previousHash;

    public MineBlockResponse(
            String message,
            long index,
            List<Transaction> transactions,
            long proof,
            String previousHash
    ) {
        this.message = message;
        this.index = index;
        this.transactions = transactions;
        this.proof = proof;
        this.previousHash = previousHash;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public long getProof() {
        return proof;
    }

    public void setProof(long proof) {
        this.proof = proof;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
}
