package br.com.jefersonmbs.recargapaywallet.domain.factory;

import br.com.jefersonmbs.recargapaywallet.domain.strategy.TransactionStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.impl.DepositStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.impl.TransferStrategy;
import br.com.jefersonmbs.recargapaywallet.domain.strategy.impl.WithdrawStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TransactionStrategyFactory {
    
    private final DepositStrategy depositStrategy;
    private final WithdrawStrategy withdrawStrategy;
    private final TransferStrategy transferStrategy;
    
    public TransactionStrategy getDepositStrategy() {
        return depositStrategy;
    }
    
    public TransactionStrategy getWithdrawStrategy() {
        return withdrawStrategy;
    }
    
    public TransactionStrategy getTransferStrategy() {
        return transferStrategy;
    }
}