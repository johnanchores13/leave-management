Gestione Ferie e Permessi

Questo progetto è un'applicazione web full-stack sviluppata per digitalizzare e semplificare il processo aziendale di richiesta, approvazione e tracciamento di ferie e permessi.

Tecnologie Utilizzate
L'architettura è divisa tra Backend (API RESTful) e Frontend (Single Page Application).

Backend:
- Java 21
- Spring Boot
- Spring Security con JWT (Autenticazione stateless)
- Spring Data JPA / Hibernate
- MySQL

Frontend:
- Angular (architettura Standalone Components)
- TypeScript & RxJS
- CSS & SweetAlert2 

---

  Funzionalità per Ruolo

Il sistema prevede tre livelli di accesso:

1. Dipendente 
   - Inserimento richieste di ferie (in giorni) o permessi (in ore).
   - Validazione automatica dei saldi disponibili al momento della richiesta.
   - Dashboard per monitorare lo stato delle proprie richieste e lo storico.

2. Responsabile
   - Visualizzazione delle richieste in sospeso del proprio team diretto.
   - Possibilità di approvare (scala il saldo in automatico) o rifiutare (obbligo di motivazione).
   - Calendario condiviso mensile per monitorare le assenze del team ed evitare accavallamenti.

3. Amministratore (HR / Admin)
   - Gestione anagrafica dipendenti, ruoli e gerarchie (chi risponde a chi).
   - Inizializzazione e monitoraggio dei saldi annuali globali.
   - Configurazione di reparti e festività aziendali (ignorate dal conteggio delle ferie).

---

Setup Locale

1. Avvio del Database
Il progetto include un "docker-compose.yml" preconfigurato. Dalla root del progetto:

docker-compose up -d
(In assenza di Docker, creare un database MySQL chiamato leave_management. Il sistema utilizzerà le credenziali di fallback definite nell'application.properties).

2. Avvio del Backend
Il server Spring Boot sarà esposto sulla porta 8080.

cd leave-management-backend
mvn spring-boot:run
(Al primo avvio, la classe DatabaseSeeder popolerà automaticamente il database vuoto creando le tabelle, i reparti di default e gli utenti di test con i relativi saldi iniziali.)

3. Avvio del Frontend
Il client Angular si aprirà in automatico su http://localhost:4200.

cd leave-management-frontend
npm install
ng serve -o

Per valutare la piattaforma, il sistema viene fornito con i seguenti account fittizi pre-caricati.

La password per tutti gli account è: Password123!

Amministratore: admin@exprivia.it
Responsabili (Manager): mario.rossi@exprivia.it | luigi.verdi@exprivia.it
Dipendenti: giulia.bianchi@exprivia.it | luca.neri@exprivia.it

