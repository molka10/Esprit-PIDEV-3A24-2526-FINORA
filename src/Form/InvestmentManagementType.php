<?php

namespace App\Form;

use App\Entity\InvestmentManagement;
use App\Entity\Investment;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

// TYPES
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextType;

class InvestmentManagementType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder

            // 🔥 INVESTMENT
            ->add('investment', EntityType::class, [
                'class' => Investment::class,
                'choice_label' => function ($inv) {
                    return $inv->getName() . ' (' . $inv->getEstimatedValue() . ' TND)';
                },
                'placeholder' => '--- Select Investment ---',
                'required' => false,
                'attr' => ['class' => 'form-control']
            ])

            // 🔥 TYPE
            ->add('investmentType', TextType::class, [
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Real Estate, Startup...'
                ]
            ])

            // 🔥 AMOUNT
            ->add('amountInvested', NumberType::class, [
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Amount (TND)'
                ]
            ])

            // 🔥 PERCENTAGE
            ->add('ownershipPercentage', NumberType::class, [
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => '0 - 100'
                ]
            ])

            // 🔥 DATE
            ->add('startDate', DateType::class, [
                'required' => false,
                'widget' => 'single_text',
                'attr' => [
                    'class' => 'form-control'
                ]
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => InvestmentManagement::class,
        ]);
    }
}