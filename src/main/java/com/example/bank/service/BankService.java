package com.example.bank.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service   // 🔥 AJOUTE CETTE LIGNE
public class BankService {

    // Représentation interne d'un compte
    public static class Account {
        public String accountId;
        public String owner;
        public BigDecimal balance;
        public String currency;

        public Account(String accountId, String owner, BigDecimal balance, String currency) {
            this.accountId = accountId;
            this.owner = owner;
            this.balance = balance;
            this.currency = currency;
        }
    }

    // Résultat du transfert
    public static class TransferResult {
        public BigDecimal fromNewBalance;
        public BigDecimal toNewBalance;
    }

    private final Map<String, Account> accounts = new HashMap<>();

    // Exemple de comptes initiaux
    public BankService() {
        accounts.put("A100", new Account("A100", "Alice", new BigDecimal("1000.00"), "EUR"));
        accounts.put("B200", new Account("B200", "Bob", new BigDecimal("500.00"), "EUR"));
    }

    // Récupérer un compte
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    // Déposer de l'argent
    public BigDecimal deposit(String accountId, BigDecimal amount) {
        Account acc = getAccount(accountId);
        if (acc == null) throw new UnknownAccountException("Account not found: " + accountId);
        acc.balance = acc.balance.add(amount);
        return acc.balance;
    }

    // Retirer de l'argent
    public BigDecimal withdraw(String accountId, BigDecimal amount) {
        Account acc = getAccount(accountId);
        if (acc == null) throw new UnknownAccountException("Account not found: " + accountId);
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || acc.balance.compareTo(amount) < 0) {
            throw new RuntimeException("Invalid withdraw amount");
        }
        acc.balance = acc.balance.subtract(amount);
        return acc.balance;
    }

    // Transférer de l'argent
    public TransferResult transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        Account from = getAccount(fromAccountId);
        Account to = getAccount(toAccountId);
        if (from == null || to == null) throw new UnknownAccountException("One of the accounts does not exist.");
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || from.balance.compareTo(amount) < 0) {
            throw new RuntimeException("Invalid transfer amount or insufficient balance.");
        }

        from.balance = from.balance.subtract(amount);
        to.balance = to.balance.add(amount);

        TransferResult result = new TransferResult();
        result.fromNewBalance = from.balance;
        result.toNewBalance = to.balance;
        return result;
    }

    // Exception pour compte inconnu
    public static class UnknownAccountException extends RuntimeException {
        public UnknownAccountException(String message) {
            super(message);
        }
    }
}
