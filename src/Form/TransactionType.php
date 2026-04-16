<?php

namespace App\Form;

use App\Entity\TransactionWallet;
use App\Entity\Category;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\Extension\Core\Type\SubmitType;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Doctrine\ORM\EntityRepository;
class TransactionType extends AbstractType
{
     public function buildForm(FormBuilderInterface $builder, array $options)
{
    $builder
        ->add('nomTransaction');

    if ($options['show_type']) {
    $builder->add('type', ChoiceType::class, [
        'choices' => [
            'Income' => 'INCOME',
            'Outcome' => 'OUTCOME',
        ],
        'expanded' => true,
        'multiple' => false,
        'data' => $options['type'], // 🔥 الحل
    ]);
}

    $builder
    ->add('montant')
    ->add('dateTransaction'); 

if ($options['show_category']) {
    $builder->add('category', EntityType::class, [
    'class' => Category::class,
    'choice_label' => 'nom',
    'query_builder' => function (EntityRepository $er) use ($options) {
        return $er->createQueryBuilder('c')
            ->where('c.type = :type')
            ->setParameter('type', $options['type']);
    },
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