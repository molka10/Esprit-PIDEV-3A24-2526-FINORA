<?php

namespace App\Dto;

class TransactionStatsDto
{
    public function __construct(
        public readonly string $type,
        public readonly float $totalAmount,
        public readonly int $count
    ) {}
}
