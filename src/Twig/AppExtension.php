<?php

namespace App\Twig;

use App\Service\CurrencyService;
use Twig\Extension\AbstractExtension;
use Twig\TwigFilter;

class AppExtension extends AbstractExtension
{
    public function __construct() {}

    public function getFilters(): array
    {
        return [];
    }
}
