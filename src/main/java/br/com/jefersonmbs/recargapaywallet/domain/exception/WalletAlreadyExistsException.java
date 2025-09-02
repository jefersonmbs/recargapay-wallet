package br.com.jefersonmbs.recargapaywallet.domain.exception;

public class WalletAlreadyExistsException extends WalletDomainException {
    
    public WalletAlreadyExistsException(Long userId) {
        super(String.format("Wallet already exists for user ID: %d", userId));
    }
    
}