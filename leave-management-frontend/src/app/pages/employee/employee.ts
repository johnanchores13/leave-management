import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { LeaveRequestService } from '../../services/leave-request';
import { AuthService } from '../../services/auth.service';
import { Richiesta } from '../../models/richiesta.model';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FormatDataPipe } from '../../pipes/format-data.pipe';

@Component({
  selector: 'app-employee',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule],
  templateUrl: './employee.html',
  styleUrl: './employee.css'
})
export class EmployeeComponent implements OnInit {


  listaRichieste: Richiesta[] = [];
  saldo: any[] = [];
  employeeId: number | null = null;
  activeSection: string = 'nuova';
  oggi: string = new Date().toISOString().split('T')[0];
  isSubmitted = false;
  profilo: any = null;

  isMenuOpen = false;
  isNotificationsModalOpen = false;

  managerUnreadCount = 0;
  managerUnreadList: Richiesta[] = [];

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu() {
    this.isMenuOpen = false;
  }

  apriNotifiche() {
    this.isNotificationsModalOpen = true;
    this.isMenuOpen = false;
  }

  chiudiNotifiche() {
    const unread = this.storicoRichieste.filter(r => r.readByEmployee === false);
    if (unread.length > 0) {
      unread.forEach(r => {
        this.leaveRequestService.segnaComeLetta(r.requestId!).subscribe();
        r.readByEmployee = true;
      });
      this.cdr.detectChanges();
    }
    this.isNotificationsModalOpen = false;
  }

  cambiaSezione(sezione: string) {
    this.activeSection = sezione;
    this.closeMenu();
  }

  constructor(private leaveRequestService: LeaveRequestService, private cdr: ChangeDetectorRef, public authService: AuthService, private router: Router) { }

  private pollingInterval: any;

  ngOnInit() {
    this.employeeId = this.authService.getEmployeeId();
    if (this.employeeId) {
      this.caricaRichieste();
      this.caricaSaldo();
      this.caricaProfilo();
      if (this.authService.getRole() === 'RESPONSABILE') {
        this.caricaRichiesteManager();
      }
      this.pollingInterval = setInterval(() => {
        this.caricaRichiesteSilenziosamente();
        if (this.authService.getRole() === 'RESPONSABILE') {
          this.caricaRichiesteManager();
        }
      }, 60000);
    }
  }

  ngOnDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  caricaProfilo() {
    this.leaveRequestService.getProfilo().subscribe({
      next: data => {
        this.profilo = data;
        this.cdr.detectChanges();
      }
    });
  }

  caricaRichiesteSilenziosamente() {
    if (!this.employeeId) return;
    this.leaveRequestService.getRichieste()
      .subscribe({
        next: data => {
          this.listaRichieste = data;
          this.cdr.detectChanges();
        }
      });
  }

  caricaRichieste() {
    if (!this.employeeId) return;
    this.leaveRequestService.getRichieste()
      .subscribe({
        next: data => {
          this.listaRichieste = data;
          this.cdr.detectChanges();
        },
        error: err => console.error('Errore caricamento richieste:', err)
      });
  }


  richiestaForm = new FormGroup({
    leaveType: new FormControl('VACATION', Validators.required),
    startDate: new FormControl(''),
    endDate: new FormControl(''),
    permitDate: new FormControl(''),
    startTime: new FormControl(''),
    endTime: new FormControl(''),
    reason: new FormControl('')
  });

  inviaRichiesta() {
    this.isSubmitted = true;
    if (this.richiestaForm.invalid) {
      Swal.fire('Attenzione', 'Compila tutti i campi obbligatori.', 'warning');
      return;
    }
    const formValue = this.richiestaForm.value;
    let richiesta: any = { leaveType: formValue.leaveType, reason: formValue.reason };

    if (formValue.leaveType === 'VACATION') {
      richiesta.startDate = formValue.startDate;
      richiesta.endDate = formValue.endDate;
    } else {
      richiesta.startDate = `${formValue.permitDate} ${formValue.startTime}`;
      richiesta.endDate = `${formValue.permitDate} ${formValue.endTime}`;
    }

    this.leaveRequestService.inviaRichiesta(richiesta).subscribe({
      next: () => {
        Swal.fire('Inviata.', 'La tua richiesta è stata inviata con successo.', 'success');
        this.caricaRichieste();
        this.caricaSaldo();
      },
      error: (err) => {
        let errorMsg = 'Errore sconosciuto';
        if (typeof err.error === 'string') {
          errorMsg = err.error;
        } else if (err.error.message) {
          errorMsg = err.error.message;
        } else if (typeof err.error === 'object') {
          errorMsg = Object.values(err.error).join('\n');
        }
        Swal.fire({
          title: 'Richiesta respinta.',
          html: errorMsg,
          icon: 'error'
        });

      }
    });
  }


  caricaSaldo() {
    if (!this.employeeId) return;
    this.leaveRequestService.getSaldo()
      .subscribe({
        next: data => {
          this.saldo = data;
          this.cdr.detectChanges();
        },
        error: err => console.error('Errore saldo:', err)
      });
  }

  annullaRichiesta(requestId: number) {
    Swal.fire({
      title: 'Sei sicuro?',
      text: 'Questa azione annullerà la richiesta in modo definitivo.',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#e74c3c',
      confirmButtonText: 'Sì, annulla',
      cancelButtonText: 'No, torna indietro'
    }).then((result) => {
      if (result.isConfirmed) {
        this.leaveRequestService.annullaRichiesta(requestId).subscribe({
          next: () => {
            Swal.fire('Annullata.', 'La richiesta è stata cancellata.', 'success');
            this.caricaRichieste();
            this.caricaSaldo();
          },
          error: (err) => {
            const msg = typeof err.error === 'string' ? err.error : 'Errore durante la cancellazione.';
            Swal.fire('Errore', msg, 'error');
          }
        });
      }
    });
  }


  leggiNotifica(id: number) {
    this.leaveRequestService.segnaComeLetta(id).subscribe({
      next: () => {
        const req = this.listaRichieste.find(r => r.requestId === id);
        if (req) {
          req.readByEmployee = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => console.error('Errore durante la lettura notifica:', err)
    });
  }

  caricaRichiesteManager() {
    this.leaveRequestService.getRichiesteByManager().subscribe({
      next: data => {
        const inAttesa = data.filter(r => r.leaveStatus === 'PENDING');
        this.managerUnreadList = inAttesa.filter(r => !r.readByManager);
        this.managerUnreadCount = this.managerUnreadList.length;
        this.cdr.detectChanges();
      }
    });
  }

  get notificheNonLette() {
    return this.storicoRichieste.filter(r => r.readByEmployee === false).length + this.managerUnreadCount;
  }

  get notificheDipendente() {
    return this.storicoRichieste.filter(r => r.readByEmployee === false).length;
  }

  get richiesteInAttesa() {
    return this.listaRichieste.filter(r => r.leaveStatus === 'PENDING');
  }

  get storicoRichieste() {
    return this.listaRichieste.filter(r => r.leaveStatus !== 'PENDING');
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  oldPassword = '';
  newPassword = '';

  cambiaPassword() {
    if (!this.oldPassword || !this.newPassword) {
      Swal.fire('Attenzione', 'Compila entrambi i campi.', 'warning');
      return;
    }
    this.leaveRequestService.cambiaPassword(this.oldPassword, this.newPassword).subscribe({
      next: () => {
        Swal.fire('Fatto', 'La password è stata aggiornata.', 'success');
        this.oldPassword = '';
        this.newPassword = '';
      },
      error: (err) => {
        let msg = 'Errore durante il cambio password.';
        try {
          const parsed = typeof err.error === 'string' ? JSON.parse(err.error) : err.error;
          msg = parsed.message || msg;
        } catch {
          if (typeof err.error === 'string') msg = err.error;
        }
        Swal.fire('Errore', msg, 'error');
      }
    });
  }

  get saldoAnnoCorrente() {
    const anno = new Date().getFullYear();
    return this.saldo.filter(s => s.referenceYear === anno);
  }

  arrotondaPerDifetto(n: number): number {
    return Math.floor(n);
  }

  private formatDataPipe = new FormatDataPipe();

  formatPeriodo(r: Richiesta): string {
    if (r.leaveType === 'VACATION') {
      return `${this.formatDataPipe.transform(r.startDate)} → ${this.formatDataPipe.transform(r.endDate)}`;
    } else {
      const data = this.formatDataPipe.transform(r.startDate);
      const oraInizio = r.startDate.substring(11);
      const oraFine = r.endDate.substring(11);
      return `${data} • ${oraInizio} - ${oraFine}`;
    }
  }
}