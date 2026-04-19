<?php

namespace App\Twig;

use App\Entity\Investment;
use App\Enum\InvestmentCategory;
use Twig\Extension\AbstractExtension;
use Twig\TwigFunction;

final class InvestmentExtension extends AbstractExtension
{
    public function getFunctions(): array
    {
        return [
            new TwigFunction('investment_image_src', $this->investmentImageSrc(...)),
            new TwigFunction('investment_category_label', $this->investmentCategoryLabel(...)),
        ];
    }

    public function investmentImageSrc(?Investment $investment): ?string
    {
        if (!$investment) {
            return null;
        }
        $v = $investment->getImageUrl();
        if ($v === null || $v === '') {
            return null;
        }
        if (str_contains($v, '://')) {
            return $v;
        }

        return '/uploads/investments/'.$v;
    }

    public function investmentCategoryLabel(?string $code): string
    {
        if ($code === null || $code === '') {
            return '—';
        }
        $enum = InvestmentCategory::tryFrom($code);

        return $enum?->label() ?? $code;
    }
}
