package com.example.demo.blockchain;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Chain {
    protected List<Block> blocks;
    protected List<Transaction> currentTransactions;
    protected Map<String, Integer> nodes;

    public Chain() throws NoSuchAlgorithmException {
        this.blocks = new ArrayList<>();
        this.currentTransactions = new ArrayList<>();
        this.nodes = new HashMap<>();

        this.AddBlock(100, "1");
    }

    public List<Block> getBlocks() {
        return this.blocks;
    }

    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    public Map<String, Integer> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, Integer> nodes) {
        this.nodes = nodes;
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

    public void RegisterNode(String address) throws MalformedURLException {
        URL url = new URL(address);
        String host = url.getHost();

        if (!this.nodes.containsKey(host)) {
            this.nodes.put(host, 0);
        }
    }

    public boolean ValidateChain(Chain chain) throws NoSuchAlgorithmException {
        Block lastBlock = chain.blocks.getFirst();
        int currentIndex = 1;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        while (currentIndex < chain.blocks.size()) {
            Block block = chain.blocks.get(currentIndex);

            if (!block.getPreviousHash().equals(this.GenerateHash(lastBlock))) {
                return false;
            }

            if (!this.validateProof(lastBlock.getProof(), this.longToBytes(block.getProof()), digest)) {
                return false;
            }

            lastBlock = block;
            currentIndex = currentIndex + 1;
        }

        return true;
    }

    public boolean ResolveConflicts() throws IOException, NoSuchAlgorithmException {
        int maxSize = this.blocks.size();
        Chain newChain = null;

        for (String nodeAddress : this.nodes.keySet()) {
            Chain remoteChain = this.getChainDataFromNode(nodeAddress);

            if (remoteChain == null) {
                continue;
            }

            if (remoteChain.blocks.size() <= maxSize) {
                continue;
            }

            if (!this.ValidateChain(remoteChain)) {
                continue;
            }

            maxSize = remoteChain.blocks.size();
            newChain = remoteChain;
        }

        if (newChain != null) {
            this.blocks = newChain.blocks;
            return true;
        }

        return false;
    }

    private Chain getChainDataFromNode(String nodeAddress) {
        try {
            URL url = new URL(String.format("http://%s/chain", nodeAddress));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            if (status != 200) {
                return null;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content.toString(), Chain.class);
        } catch (Exception e) {
            return null;
        }
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