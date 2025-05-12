package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.blockchain.Chain;
import com.example.demo.blockchain.Block;
import com.example.demo.blockchain.Transaction;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
public class MainController {
    private Chain chain;
    private UUID nodeId;

    public MainController() throws NoSuchAlgorithmException {
        this.chain = new Chain();
        this.nodeId = UUID.randomUUID();
    }

    @GetMapping("/")
    public String index() {
        return "you tip it on the side, CHELLO... it's a bass";
    }

    @GetMapping("/mine")
    public MineBlockResponse MineBlock() throws NoSuchAlgorithmException {
        Block lastBlock = this.chain.GetLastBlock();
        long lastProof = lastBlock.getProof();
        long newProof = this.chain.DoProofOfWork(lastProof);

        this.chain.AddTransaction("0", this.nodeId.toString(), 1);
        String previousHash = this.chain.GenerateHash(lastBlock);
        Block newBlock = this.chain.AddBlock(newProof, previousHash);

        return new MineBlockResponse(
                "New block created!",
                newBlock.getIndex(),
                Arrays.stream(newBlock.getTransactions()).toList(),
                newBlock.getProof(),
                newBlock.getPreviousHash()
        );
    }

    @PostMapping("/transactions/new")
    public String NewTransaction(@RequestBody Transaction transaction) {
        if (transaction.sender == null || transaction.recipient == null) {
            return "Transaction not added. Values are invalid.";
        }

        long index = this.chain.AddTransaction(transaction.sender, transaction.recipient, transaction.amount);

        return String.format("Transaction will be added to Block %d", index);
    }

    @GetMapping("/chain")
    public Chain GetChain() {
        return this.chain;
    }

    @PostMapping("/nodes/register")
    public String RegisterNode(@RequestBody List<String> nodeAddresses) throws MalformedURLException {
        for (String nodeAddress : nodeAddresses) {
            this.chain.RegisterNode(nodeAddress);
        }

        return "Nodes registered";
    }

    @GetMapping("/nodes/resolve")
    public String ResolveChainConflicts() {
        return "bob";
    }
}