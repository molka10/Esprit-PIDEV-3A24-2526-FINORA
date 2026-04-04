<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Email;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Regex;

class UserType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('email', EmailType::class, [
                'label' => 'Email *',
                'attr' => ['class' => 'form-control', 'placeholder' => 'exemple@email.com'],
                'constraints' => [
                    new NotBlank(['message' => "L'email est obligatoire"]),
                    new Email(['message' => "L'email '{{ value }}' n'est pas valide"]),
                ],
            ])
            ->add('nom', TextType::class, [
                'label' => 'Nom *',
                'attr' => ['class' => 'form-control', 'placeholder' => 'Nom complet'],
                'constraints' => [
                    new NotBlank(['message' => 'Le nom est obligatoire']),
                    new Length([
                        'min' => 2,
                        'max' => 150,
                        'minMessage' => 'Le nom doit contenir au moins {{ limit }} caractères',
                        'maxMessage' => 'Le nom ne peut pas dépasser {{ limit }} caractères',
                    ]),
                ],
            ])
            ->add('telephone', TextType::class, [
                'label' => 'Téléphone',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => 'Ex: +216 12 345 678'],
                'constraints' => [
                    new Regex([
                        'pattern' => '/^[0-9+\s\-()]{8,20}$/',
                        'message' => 'Le numéro de téléphone est invalide (8-20 chiffres)',
                    ]),
                ],
            ])
            ->add('roles', ChoiceType::class, [
                'label' => 'Rôle *',
                'choices' => [
                    'Administrateur' => 'ROLE_ADMIN',
                    'Entreprise' => 'ROLE_ENTREPRISE',
                    'Utilisateur' => 'ROLE_USER',
                ],
                'multiple' => true,
                'expanded' => true,
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'Le rôle est obligatoire']),
                ],
            ])
            ->add('password', PasswordType::class, [
                'label' => 'Mot de passe *',
                'attr' => ['class' => 'form-control', 'placeholder' => 'Minimum 6 caractères'],
                'constraints' => [
                    new NotBlank(['message' => 'Le mot de passe est obligatoire']),
                    new Length([
                        'min' => 6,
                        'minMessage' => 'Le mot de passe doit contenir au moins {{ limit }} caractères',
                    ]),
                ],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}