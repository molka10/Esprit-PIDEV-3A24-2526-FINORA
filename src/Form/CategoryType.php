<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;

/**
 * Category Form Type
 */
class CategoryType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options)
    {
        $builder
            ->add('nom')
            ->add('type', ChoiceType::class, [
                'choices' => [
                    'Income' => 'INCOME',
                    'Outcome' => 'OUTCOME',
                ],
            ])
            ->add('priorite', ChoiceType::class, [
                'choices' => [
                    'Haute' => 'HAUTE',
                    'Moyenne' => 'MOYENNE',
                    'Basse' => 'BASSE',
                ],
            ]);
    }

}