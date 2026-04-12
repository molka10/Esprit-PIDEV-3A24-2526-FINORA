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
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;

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
                'placeholder' => '--- Sélectionner un investissement ---',
                'required' => true,
                'attr' => ['class' => 'form-select']
            ])

            // 🔥 TYPE
            ->add('investmentType', TextType::class, [
                'label' => 'Type d’investissement',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Immobilier, Startup...'
                ]
            ])

            // 🔥 AMOUNT
            ->add('amountInvested', NumberType::class, [
                'label' => 'Montant investi (TND)',
                'required' => true,
                'scale' => 2,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: 50000',
                    'min' => 0,
                    'step' => '0.01'
                ]
            ])

            // 🔥 PERCENTAGE
            ->add('ownershipPercentage', NumberType::class, [
                'label' => 'Pourcentage (%)',
                'required' => true,
                'scale' => 2,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: 25',
                    'min' => 0,
                    'max' => 100,
                    'step' => '0.01'
                ]
            ])

            // 🔥 DATE
            ->add('startDate', DateType::class, [
                'label' => 'Date de début',
                'required' => true,
                'widget' => 'single_text',
                'attr' => [
                    'class' => 'form-control'
                ]
            ])

            // 🔥 STATUS (AJOUT IMPORTANT)
            ->add('status', ChoiceType::class, [
                'label' => 'Statut',
                'choices' => [
                    'Actif' => 'ACTIVE',
                    'Clôturé' => 'CLOSED',
                ],
                'placeholder' => 'Choisir un statut',
                'required' => true,
                'attr' => ['class' => 'form-select']
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => InvestmentManagement::class,
            'attr' => [
                'class' => 'management-form',
                'novalidate' => 'novalidate',
            ],
        ]);
    }
}