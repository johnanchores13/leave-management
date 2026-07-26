Gestione Ferie e Permessi

Applicazione web full-stack per gestire richieste, approvazioni e tracciamento di ferie e permessi aziendali. L'ho sviluppata durante il mio stage in Exprivia come esercizio per semplificare un processo aziendale.

Il progetto è diviso in due parti: un backend che espone API REST e un frontend Angular che ci si collega come Single Page Application.

Stack

Backend

- Java 21
- Spring Boot
- Spring Security con JWT (autenticazione stateless, senza sessioni sul server)
- Spring Data JPA / Hibernate
- MySQL

Frontend

- Angular (standalone components)
- TypeScript & RxJS
- CSS

Ci sono tre ruoli con permessi diversi:

Dipendente

- Richiede ferie (in giorni) o permessi (in ore)
- Il sistema controlla in automatico che il saldo disponibile sia sufficiente prima di accettare la richiesta
- Dashboard con storico e stato delle proprie richieste

Responsabile

- Vede le richieste in sospeso del proprio team diretto
- Può approvare (il saldo si aggiorna in automatico) o rifiutare (deve motivare)
- Calendario mensile condiviso per controllare le assenze del team ed evitare che troppe persone siano fuori nello stesso periodo

Amministratore

- Gestisce anagrafica dipendenti, ruoli e gerarchie (chi risponde a chi)
- Inizializza e monitora i saldi annuali
- Configura reparti e festività aziendali (escluse dal conteggio ferie)

1) Database

Il repo include un docker-compose.yml già pronto:

(bash)
docker-compose up -d

Se non hai Docker, crea a mano un database MySQL chiamato leave_management — il sistema userà le credenziali di fallback già impostate in application.properties.

2) Backend (porta 8080)

(bash)
cd leave-management-backend
mvn spring-boot:run

Al primo avvio, un DatabaseSeeder popola il database vuoto: crea le tabelle, i reparti di default e degli utenti di test con saldi già impostati.

3) Frontend (localhost:4200)

(bash)
cd leave-management-frontend
npm install
ng serve -o

Account di test

Per provare la piattaforma senza registrare nulla, sono già caricati questi account (password per tutti: Password123!):

Admin: admin@exprivia.it
Manager: mario.rossi@exprivia.it, luigi.verdi@exprivia.it
Dipendenti: giulia.bianchi@exprivia.it, luca.neri@exprivia.it
