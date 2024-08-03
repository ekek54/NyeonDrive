package com.example.nyeondrive.file.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FileClosure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_closure_id")
    private Long id;

    @JoinColumn(name = "ancestor_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private File ancestor;

    @JoinColumn(name = "descendant_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private File descendant;

    @Column(name = "depth", nullable = false)
    private Long depth;

    @Builder
    private FileClosure(File ancestor, File descendant, Long depth) {
        this.ancestor = ancestor;
        this.descendant = descendant;
        this.depth = depth;
    }

    public boolean isImmediate() {
        return this.depth == 1;
    }

    public boolean isNotSelf() {
        return this.depth != 0;
    }

    @Override
    public String toString() {
        return "FileClosure{" +
                "id=" + id +
                ", ancestor=" + ancestor.getId() +
                ", descendant=" + descendant.getId() +
                ", depth=" + depth +
                '}';
    }
}
