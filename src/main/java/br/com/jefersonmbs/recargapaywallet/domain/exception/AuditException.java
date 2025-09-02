package br.com.jefersonmbs.recargapaywallet.domain.exception;


public class AuditException extends RuntimeException {

    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }

}