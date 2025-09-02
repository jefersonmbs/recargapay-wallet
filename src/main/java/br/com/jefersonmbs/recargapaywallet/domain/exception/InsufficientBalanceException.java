package br.com.jefersonmbs.recargapaywallet.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends WalletDomainException {
    
    public InsufficientBalanceException(BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient balance for transaction. Available: %s, Requested: %s", 
              available, requested));
    }
    
}