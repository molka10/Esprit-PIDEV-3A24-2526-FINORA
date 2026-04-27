<?php

namespace App\Twig;

use App\Service\WalletBalanceService;
use Symfony\Bundle\SecurityBundle\Security;
use Twig\Extension\AbstractExtension;
use Twig\TwigFunction;

class WalletExtension extends AbstractExtension
{
    private WalletBalanceService $walletBalanceService;
    private Security $security;
    private \App\Service\CurrencyConverterService $currencyConverter;
    private \Symfony\Component\HttpFoundation\RequestStack $requestStack;

    public function __construct(
        WalletBalanceService $walletBalanceService, 
        Security $security,
        \App\Service\CurrencyConverterService $currencyConverter,
        \Symfony\Component\HttpFoundation\RequestStack $requestStack
    ) {
        $this->walletBalanceService = $walletBalanceService;
        $this->security = $security;
        $this->currencyConverter = $currencyConverter;
        $this->requestStack = $requestStack;
    }

    public function getFunctions(): array
    {
        return [
            new TwigFunction('wallet_balance', [$this, 'getWalletBalance']),
            new TwigFunction('current_currency', [$this, 'getCurrentCurrency']),
            new TwigFunction('convert_currency', [$this, 'convertCurrency']),
        ];
    }

    public function convertCurrency(float $amount, string $toCurrency): float
    {
        return $this->currencyConverter->convert($amount, 'TND', $toCurrency);
    }

    public function getCurrentCurrency(): string
    {
        return $this->requestStack->getSession()->get('app_currency', 'TND');
    }

    public function getWalletBalance(): float
    {
        $user = $this->security->getUser();
        if (!$user) {
            return 0.0;
        }

        // Return raw balance in TND. The Twig filter |format_currency will handle conversion and symbols.
        return $this->walletBalanceService->calculateUserBalance($user->getId());
    }
}
