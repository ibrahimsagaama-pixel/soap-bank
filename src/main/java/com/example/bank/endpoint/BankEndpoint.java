package com.example.bank.endpoint;

import java.math.BigDecimal;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.bank.service.BankService;
import com.example.bank.service.BankService.Account;
import com.example.bank.service.UnknownAccountException;
import com.example.bank.ws.AccountType;
import com.example.bank.ws.DepositRequest;
import com.example.bank.ws.DepositResponse;
import com.example.bank.ws.GetAccountRequest;
import com.example.bank.ws.GetAccountResponse;
import com.example.bank.ws.WithdrawRequest;
import com.example.bank.ws.WithdrawResponse;
import com.example.bank.ws.TransferRequest;
import com.example.bank.ws.TransferResponse;


@Endpoint
public class BankEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/bank";
    private final BankService bankService;

    public BankEndpoint(BankService bankService) {
        this.bankService = bankService;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAccountRequest")
    @ResponsePayload
    public GetAccountResponse getAccount(@RequestPayload GetAccountRequest request) {
        Account acc = bankService.getAccount(request.getAccountId());
        if (acc == null) {
            throw new UnknownAccountException("Unknown accountId: " + request.getAccountId());
        }

        AccountType dto = new AccountType();
        dto.setAccountId(acc.accountId);
        dto.setOwner(acc.owner);
        dto.setBalance(acc.balance);
        dto.setCurrency(acc.currency);

        GetAccountResponse resp = new GetAccountResponse();
        resp.setAccount(dto);
        return resp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DepositRequest")
    @ResponsePayload
    public DepositResponse deposit(@RequestPayload DepositRequest request) {
        BigDecimal newBalance = bankService.deposit(request.getAccountId(), request.getAmount());
        DepositResponse resp = new DepositResponse();
        resp.setNewBalance(newBalance);
        return resp;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "WithdrawRequest")
    @ResponsePayload
    public WithdrawResponse withdraw(@RequestPayload WithdrawRequest request) {
        Account account = bankService.getAccount(request.getAccountId());

        if (account == null) {
            throw new UnknownAccountException("Account not found: " + request.getAccountId());
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0 || account.balance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Invalid withdraw amount");
        }

        BigDecimal newBalance = bankService.withdraw(request.getAccountId(), request.getAmount());

        WithdrawResponse response = new WithdrawResponse();
        response.setNewBalance(newBalance);
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "TransferRequest")
@ResponsePayload
public TransferResponse transfer(@RequestPayload TransferRequest request) {
    BankService.TransferResult result = bankService.transfer(
        request.getFromAccountId(),
        request.getToAccountId(),
        request.getAmount()
    );

    TransferResponse response = new TransferResponse();
    response.setFromNewBalance(result.fromNewBalance);
    response.setToNewBalance(result.toNewBalance);

    return response;
}

    
}

