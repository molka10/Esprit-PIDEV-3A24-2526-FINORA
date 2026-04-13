<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity]
#[ORM\Table(name: 'quiz_result')]
class QuizResult
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 255, nullable: false)]
    private ?string $studentName = null;

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $lessonId = null;

    #[ORM\Column(type: 'string', length: 255, nullable: false)]
    private ?string $lessonTitle = null;

    #[ORM\Column(type: 'string', length: 255, nullable: false)]
    private ?string $formationTitle = null;

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $score = null;

    #[ORM\Column(type: 'integer', nullable: false)]
    private ?int $passed = null;

    #[ORM\Column(type: 'datetime', nullable: true)]
    private ?\DateTimeInterface $takenAt = null;

    #[ORM\Column(type: 'integer', nullable: false, options: ['default' => 0])]
    private ?int $fraudSuspected = 0;

    #[ORM\Column(type: 'text', nullable: true)]
    private ?string $fraudExplanation = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getStudentName(): ?string
    {
        return $this->studentName;
    }

    public function setStudentName(string $studentName): self
    {
        $this->studentName = trim($studentName);
        return $this;
    }

    public function getLessonId(): ?int
    {
        return $this->lessonId;
    }

    public function setLessonId(int $lessonId): self
    {
        $this->lessonId = $lessonId;
        return $this;
    }

    public function getLessonTitle(): ?string
    {
        return $this->lessonTitle;
    }

    public function setLessonTitle(string $lessonTitle): self
    {
        $this->lessonTitle = trim($lessonTitle);
        return $this;
    }

    public function getFormationTitle(): ?string
    {
        return $this->formationTitle;
    }

    public function setFormationTitle(string $formationTitle): self
    {
        $this->formationTitle = trim($formationTitle);
        return $this;
    }

    public function getScore(): ?int
    {
        return $this->score;
    }

    public function setScore(int $score): self
    {
        $this->score = $score;
        return $this;
    }

    public function getPassed(): ?int
    {
        return $this->passed;
    }

    public function setPassed(int $passed): self
    {
        $this->passed = $passed;
        return $this;
    }

    public function isPassed(): bool
    {
        return (int) $this->passed === 1;
    }

    public function getTakenAt(): ?\DateTimeInterface
    {
        return $this->takenAt;
    }

    public function setTakenAt(?\DateTimeInterface $takenAt): self
    {
        $this->takenAt = $takenAt;
        return $this;
    }

    public function getFraudSuspected(): ?int
    {
        return $this->fraudSuspected;
    }

    public function setFraudSuspected(int $fraudSuspected): self
    {
        $this->fraudSuspected = $fraudSuspected;
        return $this;
    }

    public function isFraudSuspected(): bool
    {
        return (int) $this->fraudSuspected === 1;
    }

    public function getFraudExplanation(): ?string
    {
        return $this->fraudExplanation;
    }

    public function setFraudExplanation(?string $fraudExplanation): self
    {
        $this->fraudExplanation = $fraudExplanation;
        return $this;
    }
}