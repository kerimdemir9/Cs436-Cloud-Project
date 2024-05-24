package com.customer.transaction.controller;

import com.customer.transaction.controller.View.TransactionViewPagedData;
import com.customer.transaction.controller.View.TransactionView;
import com.customer.transaction.data.model.TransactionModel;
import com.customer.transaction.data.service.CustomerService;
import com.customer.transaction.data.service.TransactionService;
import com.customer.transaction.data.util.GenericPagedModel;
import com.customer.transaction.util.SortDirection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.customer.transaction.controller.util.Parsers.*;

@RestController
@Slf4j
public class TransactionController {

    private final RestTemplate restTemplate = new RestTemplate();
    final TransactionService transactionService;
    final CustomerService customerService;

    @Autowired
    public TransactionController(TransactionService transactionService, CustomerService customerService) {
        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    // function url: https://us-central1-bankproject-424211.cloudfunctions.net/myDatabaseFunction?id=1
    @RequestMapping(value = "/v1/transactions/calculate_user_average_income/{id}", method = RequestMethod.GET)
    private ResponseEntity<Object> getUserAvgIncomeIdV1(@PathVariable String id) {
        val url = "https://us-central1-bankproject-424211.cloudfunctions.net/myDatabaseFunction?id=".concat(id);
        val req = restTemplate.exchange(url, HttpMethod.GET, null, Object.class);
        return ResponseEntity.ok(req.getBody());
    }
    

    @RequestMapping(value = "/v1/transactions/{id}", method = RequestMethod.GET)
    private ResponseEntity<TransactionView> getTransactionByIdV1(@PathVariable String id) {
        log.info("Calling: getTransactionByIdV1 >> ".concat(id));

        val result = transactionService.findById(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapTransactionToTransactionView(result));
    }

    @RequestMapping(value = "/v1/transactions/find_all", method = RequestMethod.GET)
    private ResponseEntity<TransactionViewPagedData> getAllTransactionsV1(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("Calling: getAllTransactionsV1");

        val result = transactionService.findAll(pageNo, pageSize, sortBy, SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }

    @RequestMapping(value = "/v1/transactions/find_all_by_receiver/{receiverId}", method = RequestMethod.GET)
    private ResponseEntity<TransactionViewPagedData> getAllTransactionsByReceiverV1(
            @PathVariable String receiverId,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        val customerModel = customerService.findById(tryParseInteger(receiverId, "receiverId"));

        log.info("Calling: getAllTransactionsByReceiverV1 >> Customer fullName: ".concat(customerModel.getFullName()));


        val result = transactionService.findAllByReceiver
                (customerModel, pageNo, pageSize, sortBy, SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }


    @RequestMapping(value = "/v1/transactions/find_all_by_receiver_created_before_created_after/{receiverId}", method = RequestMethod.GET)
    private ResponseEntity<TransactionViewPagedData> getAllByReceiverAndCreatedBeforeAndCreatedAfterV1(
            @PathVariable String receiverId,
            @RequestParam(defaultValue = "") String createdBefore,
            @RequestParam(defaultValue = "") String createdAfter,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        val customerModel = customerService.findById(tryParseInteger(receiverId, "receiverId"));

        log.info("Calling: getAllByReceiverAndCreatedBeforeAndCreatedAfterV1 >> Customer: "
                .concat(customerModel.toString())
                .concat(" | Created Before: ").concat(createdBefore)
                .concat(" | Created After: ").concat(createdAfter));

        val result = transactionService.findAllByReceiverAndCreatedBeforeAndCreatedAfter(customerModel,
                new Date(tryParseLong(createdBefore, "createdBefore")),
                new Date(tryParseLong(createdAfter, "createdAfter")), pageNo, pageSize, sortBy, SortDirection.of(sortDir));

        return ResponseEntity.ok(mapPaged(result));
    }


    @RequestMapping(value = "/v1/transactions/save", method = RequestMethod.POST)
    private ResponseEntity<TransactionView> saveTransactionV1(@RequestBody TransactionView transaction) {
        log.info("Calling: saveTransactionV1 >> ".concat(transaction.toString()));

        val receiver = customerService.findById(transaction.getReceiverId());
        val sender = customerService.findById(transaction.getSenderId());

        val saved = transactionService.save(TransactionModel
                .builder()
                .id(transaction.getId())
                .created(transaction.getCreated())
                .amount(transaction.getAmount())
                .sender(sender)
                .receiver(receiver)
                .build());

        return ResponseEntity.ok(mapTransactionToTransactionView(saved));
    }

    @RequestMapping(value = "/v1/transactions/delete/{id}", method = RequestMethod.DELETE)
    private ResponseEntity<TransactionView> deleteTransactionV1(@PathVariable String id) {
        log.info("Calling: deleteCustomerV1 >> ".concat(id));

        val result = transactionService.hardDelete(tryParseInteger(id, "id"));

        return ResponseEntity.ok(mapTransactionToTransactionView(result));
    }


    private TransactionViewPagedData mapPaged(GenericPagedModel<TransactionModel> transactions) {
        return TransactionViewPagedData
                .builder()
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .numberOfElements(transactions.getNumberOfElements())
                .content(mapTransactionsCollectiontoTransactionViewList(transactions.getContent()))
                .build();
    }

    private List<TransactionView> mapTransactionsCollectiontoTransactionViewList(Collection<TransactionModel> transactionModels) {
        return transactionModels.stream().map(this::mapTransactionToTransactionView).toList();
    }


    private TransactionView mapTransactionToTransactionView(TransactionModel transactionModel) {
        return TransactionView.builder()
                .amount(transactionModel.getAmount())
                .created(transactionModel.getCreated())
                .id(transactionModel.getId())
                .senderId(transactionModel.getSender().getId())
                .receiverId(transactionModel.getReceiver().getId())
                .build();
    }

}
