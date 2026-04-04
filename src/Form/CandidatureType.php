<?php

namespace App\Form;

use App\Entity\AppelOffre;
use App\Entity\Candidature;
use App\Entity\User;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Positive;
use Symfony\Component\Validator\Constraints\Length;

class CandidatureType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('appelOffre', EntityType::class, [
                'label' => "Appel d'Offre *",
                'class' => AppelOffre::class,
                'choice_label' => 'titre',
                'placeholder' => "-- Choisir un appel d'offre --",
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => "L'appel d'offre est obligatoire"]),
                ],
            ])
            ->add('user', EntityType::class, [
                'label' => 'Candidat *',
                'class' => User::class,
                'choice_label' => 'nom',
                'placeholder' => '-- Choisir un candidat --',
                'required' => true,
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'Le candidat est obligatoire']),
                ],
            ])
            ->add('montantPropose', NumberType::class, [
                'label' => 'Montant Proposé *',
                'required' => true,
                'attr' => ['class' => 'form-control', 'placeholder' => '0.00'],
                'constraints' => [
                    new NotBlank(['message' => 'Le montant proposé est obligatoire']),
                    new Positive(['message' => 'Le montant doit être positif']),
                ],
            ])
            ->add('message', TextareaType::class, [
                'label' => 'Message / Motivation *',
                'required' => true,
                'attr' => ['class' => 'form-control', 'rows' => 4, 'placeholder' => 'Décrivez votre offre...'],
                'constraints' => [
                    new NotBlank(['message' => 'Le message est obligatoire']),
                    new Length([
                        'min' => 20,
                        'minMessage' => 'Le message doit contenir au moins {{ limit }} caractères',
                    ]),
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'label' => 'Statut *',
                'choices' => [
                    'En attente' => 'submitted',
                    'Acceptée' => 'accepted',
                    'Rejetée' => 'rejected',
                ],
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'Le statut est obligatoire']),
                ],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Candidature::class,
        ]);
    }
}