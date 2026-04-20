<?php

namespace App\Form;

use App\Entity\TransactionWallet;
use App\Entity\Category;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\Form\Extension\Core\Type\HiddenType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;

class TransactionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options)
    {
        $builder->add('nomTransaction');

        if ($options['show_type']) {
            $builder->add('type', ChoiceType::class, [
                'choices' => [
                    'Income' => 'INCOME',
                    'Outcome' => 'OUTCOME',
                ],
                'expanded' => true,
                'multiple' => false,
                'data' => $options['type'],
            ]);
        }

        $builder
            ->add('montant')
            ->add('dateTransaction');

        if ($options['show_category']) {
            // Use a simple hidden field; JS will set the value dynamically.
            // The actual Category object is mapped manually in the controller.
            $builder->add('category', HiddenType::class, [
                'mapped' => false, // controlled entirely by JS + controller
                'required' => false,
            ]);
        }

        $builder->add('save', SubmitType::class);
    }

    public function configureOptions(OptionsResolver $resolver)
    {
        $resolver->setDefaults([
            'data_class' => TransactionWallet::class,
            'type' => 'INCOME',
            'show_type' => true,
            'show_category' => true,
        ]);
    }
}
