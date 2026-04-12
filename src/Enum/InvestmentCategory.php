<?php

namespace App\Enum;

enum InvestmentCategory: string
{
    case MAISON = 'MAISON';
    case STARTUP = 'STARTUP';
    case HOTEL = 'HOTEL';
    case TERRAIN = 'TERRAIN';

    public function label(): string
    {
        return match ($this) {
            self::MAISON => 'Maison',
            self::STARTUP => 'Startup',
            self::HOTEL => 'Hôtel',
            self::TERRAIN => 'Terrain',
        };
    }

    /** @return list<string> */
    public static function values(): array
    {
        return array_column(self::cases(), 'value');
    }

    /** @return array<string, string> label => value for Symfony ChoiceType */
    public static function formChoices(): array
    {
        $out = [];
        foreach (self::cases() as $c) {
            $out[$c->label()] = $c->value;
        }

        return $out;
    }

}
