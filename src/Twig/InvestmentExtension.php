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
        
        // If specific image exists, use it
        if ($v !== null && $v !== '') {
            if (str_contains($v, '://')) {
                return $v;
            }
            return '/uploads/investments/'.$v;
        }

        // Default realistic images based on category
        $category = $investment->getCategory();
        return match ($category) {
            'IMMOBILIER', 'MAISON', 'HOTEL', 'TERRAIN' => '/assets/images/investments/real_estate.png',
            'STARTUP' => '/assets/images/investments/startup.png',
            'AGRICULTURE' => '/assets/images/investments/agriculture.png',
            'ENERGIE' => '/assets/images/investments/energy.png',
            default => '/assets/images/courses/4by3/06.jpg',
        };
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
