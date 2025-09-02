package br.com.jefersonmbs.recargapaywallet.domain.service;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;

import java.util.UUID;


public interface WalletFinderService {

    WalletEntity findWalletById(UUID walletId);

    WalletEntity findTargetWallet(TransactionRequestDto request);
}