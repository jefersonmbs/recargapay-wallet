package br.com.jefersonmbs.recargapaywallet.domain.exception;

public class InactiveWalletException extends WalletDomainException {
    
    public InactiveWalletException(String message) {
        super(message);
    }
    
}