package br.com.jefersonmbs.recargapaywallet.domain.service.impl;

import br.com.jefersonmbs.recargapaywallet.api.dto.TransactionRequestDto;
import br.com.jefersonmbs.recargapaywallet.domain.entity.WalletEntity;
import br.com.jefersonmbs.recargapaywallet.domain.exception.WalletNotFoundException;
import br.com.jefersonmbs.recargapaywallet.domain.repository.WalletRepository;
import br.com.jefersonmbs.recargapaywallet.domain.service.WalletFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletFinderServiceImpl implements WalletFinderService {
    
    private final WalletRepository walletRepository;
    
    @Override
    public WalletEntity findWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
    }
    
    @Override
    public WalletEntity findTargetWallet(TransactionRequestDto request) {
        if (request.getTargetWalletId() != null) {
            return findWalletById(request.getTargetWalletId());
        } else if (request.getTargetAccountNumber() != null) {
            return walletRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new WalletNotFoundException("Target wallet not found for account number: " + 
                    request.getTargetAccountNumber()));
        } else if (request.getTargetUserCpf() != null) {
            return walletRepository.findByUserCpf(request.getTargetUserCpf())
                .orElseThrow(() -> new WalletNotFoundException("Target wallet not found for user CPF: " + 
                    request.getTargetUserCpf()));
        } else {
            throw new IllegalArgumentException("Target wallet identification is required (wallet ID, account number, or user CPF)");
        }
    }
}