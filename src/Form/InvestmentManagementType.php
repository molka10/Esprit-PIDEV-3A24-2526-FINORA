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

// VALIDATION
use Symfony\Component\Validator\Constraints as Assert;

class InvestmentManagementType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder

            // 🔥 SELECT INVESTMENT
            ->add('investment', EntityType::class, [
                'class' => Investment::class,
                'choice_label' => function ($inv) {
                    return $inv->getName() . ' (' . $inv->getEstimatedValue() . ' TND)';
                },
                'placeholder' => '--- Select Investment ---',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotNull(['message' => 'Please select an investment'])
                ]
            ])

            // 🔥 TYPE
            ->add('investmentType', TextType::class, [
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Real Estate, Startup...'
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Type is required']),
                    new Assert\Length([
                        'min' => 3,
                        'minMessage' => 'Minimum 3 characters'
                    ])
                ]
            ])

            // 🔥 AMOUNT
            ->add('amountInvested', NumberType::class, [
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Amount (TND)'
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Amount is required']),
                    new Assert\Positive(['message' => 'Amount must be positive'])
                ]
            ])

            // 🔥 PERCENTAGE
            ->add('ownershipPercentage', NumberType::class, [
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => '0 - 100'
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Percentage required']),
                    new Assert\Range([
                        'min' => 0,
                        'max' => 100,
                        'notInRangeMessage' => 'Must be between 0 and 100'
                    ])
                ]
            ])

            // 🔥 DATE
            ->add('startDate', DateType::class, [
                'widget' => 'single_text',
                'attr' => [
                    'class' => 'form-control'
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Date required'])
                ]
            ]);

        // ❌ STATUS supprimé → calculé automatiquement dans controller
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => InvestmentManagement::class,
        ]);
    }
}