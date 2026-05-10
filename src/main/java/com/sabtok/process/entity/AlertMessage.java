package com.sabtok.process.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "alert_message")
@SequenceGenerator(name = "alert_message_seq", sequenceName = "alert_message_seq", allocationSize = 1)
public class AlertMessage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_message_seq")
    @Column(name = "id")
    private Long id;
    private String message;
    private String severity;

    public AlertMessage() {
    }

    public AlertMessage(String message, String severity) {
        this.message = message;
        this.severity = severity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
