package com.example.demo.blockchain;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Chain {
    protected List<Block> blocks;
    protected List<Transaction> currentTransactions;

    public Chain() throws NoSuchAlgorithmException {
        this.blocks = new ArrayList<>();
        this.currentTransactions = new ArrayList<>();

        this.AddBlock(100, "1");
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public long AddTransaction(String sender, String recipient, double amount) {
        this.currentTransactions.add(new Transaction(
                sender,
                recipient,
                amount
        ));

        Block lastBlock = this.GetLastBlock();

        return lastBlock.getIndex() + 1;
    }

    public Block AddBlock(long proof, String previousHash) throws NoSuchAlgorithmException {
        Block block = new Block(
            this.blocks.size() + 1,
                this.getUnixCurrentTimestamp(),
                this.getCurrentTransactionsArray(),
                proof,
                previousHash
        );

        block.setPreviousHash(this.GenerateHash(block));
        this.currentTransactions.clear();
        this.blocks.add(block);

        return block;
    }

    public Block GetLastBlock() {
        int currentSize = this.blocks.size();

        return this.blocks.get(currentSize - 1);
    }

    public long DoProofOfWork(long lastProof) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        long proof = 0;

        while (!this.validateProof(lastProof, this.longToBytes(proof), digest)) {
            proof = proof + 1;
        }

        return proof;
    }

    public String GenerateHash(Block block) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream encoderBytes = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(encoderBytes);

        encoder.writeObject(block);
        encoder.close();

        byte[] hashBytes = digest.digest(encoderBytes.toByteArray());

        return this.bytesToHex(hashBytes);
    }

    private long getUnixCurrentTimestamp() {
        return System.currentTimeMillis() / 1000L;
    }

    private Transaction[] getCurrentTransactionsArray() {
        int currentSize = this.currentTransactions.size();
        Transaction[] t = new Transaction[currentSize];

        return this.currentTransactions.toArray(t);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    private byte[] longToBytes(long number) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(number);
        return buffer.array();
    }

    private boolean validateProof(long lastProof, byte[] proof, MessageDigest digest) {
        byte[] lastProofBytes = longToBytes(lastProof);
        byte[] both = Arrays.copyOf(lastProofBytes, lastProofBytes.length + proof.length);
        System.arraycopy(proof, 0, both, lastProofBytes.length, proof.length);
        byte[] hashBytes = digest.digest(both);
        String hashHex = this.bytesToHex(hashBytes);

        if (hashHex.charAt(0) == '0' &&
            hashHex.charAt(1) == '0' &&
            hashHex.charAt(2) == '0' &&
            hashHex.charAt(3) == '0'
        ) {
            return true;
        }

        return false;
    }
}