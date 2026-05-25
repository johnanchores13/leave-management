import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LeaveRequestService } from '../../services/leave-request';
import { AuthService } from '../../services/auth.service';
import { Richiesta } from '../../models/richiesta.model';
import Swal from 'sweetalert2';
import { Router } from '@angular/router';
import { RouterModule } from '@angular/router';
import { FormatDataPipe } from '../../pipes/format-data.pipe';

@Component({
  selector: 'app-manager',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './manager.html',
  styleUrl: './manager.css'
})
export class ManagerComponent implements OnInit {
  listaInAttesa: Richiesta[] = [];
  listaStorico: Richiesta[] = [];
  unreadCount: number = 0;
  pollingInterval: any;
  meseCorrente: Date = new Date();
  giorniCalendario: any[] = [];
  isMenuOpen = false;
  isNotificationsModalOpen = false;
  activeSection = 'richieste';
  giornoSelezionato: any = null;

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu() {
    this.isMenuOpen = false;
  }

  cambiaSezione(sezione: string) {
    this.activeSection = sezione;
    this.closeMenu();
  }

  apriNotifiche() {
    this.isNotificationsModalOpen = true;
    this.isMenuOpen = false;
  }

  chiudiNotifiche() {
    this.isNotificationsModalOpen = false;
  }

  constructor(private leaveRequestService: LeaveRequestService, private cdr: ChangeDetectorRef, public authService: AuthService, private router: Router) { }

  ngOnInit() {
    this.caricaRichieste();
    this.pollingInterval = setInterval(() => {
      this.caricaRichiestePolling();
    }, 60000);
  }

  ngOnDestroy() {
    if (this.pollingInterval) {
      clearInterval(this.pollingInterval);
    }
  }

  caricaRichiestePolling() {
    this.leaveRequestService.getRichiesteByManager().subscribe({
      next: data => {
        this.listaInAttesa = data.filter(r => r.leaveStatus === 'PENDING');
        this.listaStorico = data.filter(r => r.leaveStatus !== 'PENDING');
        this.generaCalendario();
        const nuove = this.listaInAttesa.filter(r => !r.readByManager);
        this.unreadCount = nuove.length;
        this.cdr.detectChanges();
      }
    });
  }

  caricaRichieste() {
    this.leaveRequestService.getRichiesteByManager().subscribe({
      next: data => {
        this.listaInAttesa = data.filter(r => r.leaveStatus === 'PENDING');
        this.listaStorico = data.filter(r => r.leaveStatus !== 'PENDING');
        this.generaCalendario();

        const nuove = this.listaInAttesa.filter(r => !r.readByManager);
        this.unreadCount = nuove.length;
        this.cdr.detectChanges();
        nuove.forEach(r => {
          this.leaveRequestService.segnaComeLettaManager(r.requestId).subscribe();
        });
      },
      error: err => console.error('Errore:', err)
    });
  }

  approva(id: number) {
    this.leaveRequestService.segnaComeLettaManager(id).subscribe();
    this.leaveRequestService.approva(id).subscribe({
      next: () => {
        Swal.fire('Fatto', 'La richiesta è stata approvata.', 'success').then(() => {
          this.caricaRichieste();
        });
      },
      error: (err) => Swal.fire('Errore', 'Si è verificato un problema', 'error')
    });
  }

  rifiuta(id: number) {
    Swal.fire({
      title: 'Rifiuta richiesta',
      html: `
        <label style="font-weight:500;color:#475569;display:flex;flex-direction:column;gap:8px;text-align:left;">
          <span>Motivo del rifiuto<span style="color:#e74c3c;margin-left:3px;font-weight:bold;">*</span></span>
          <textarea id="swal-rejection-reason" rows="3" placeholder="Scrivi un motivo..." style="padding:10px 15px;border:1px solid #cbd5e1;border-radius:6px;font-family:inherit;font-size:1rem;width:100%;box-sizing:border-box;resize:vertical;"></textarea>
          <small id="swal-rejection-error" style="color:#e74c3c;font-size:0.85em;display:none;">Il motivo è obbligatorio.</small>
        </label>
      `,
      showCancelButton: true,
      confirmButtonColor: '#d33',
      confirmButtonText: 'Rifiuta',
      cancelButtonText: 'Annulla',
      focusConfirm: false,
      preConfirm: () => {
        const reason = (document.getElementById('swal-rejection-reason') as HTMLTextAreaElement).value.trim();
        const errorEl = document.getElementById('swal-rejection-error')!;
        if (!reason) {
          errorEl.style.display = 'block';
          return false;
        }
        return reason;
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.leaveRequestService.segnaComeLettaManager(id).subscribe();
        this.leaveRequestService.rifiuta(id, result.value).subscribe({
          next: () => {
            Swal.fire('Rifiutata', 'La richiesta è stata rifiutata.', 'success').then(() => {
              this.caricaRichieste();
            });
          },
          error: (err) => Swal.fire('Errore', 'Si è verificato un problema', 'error')
        });
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
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

  vediSaldo(employeeId: number, nome: string) {
    this.leaveRequestService.getSaldoDipendentePerManager(employeeId).subscribe({
      next: (saldi) => {
        const annoCorrente = new Date().getFullYear();
        const saldoAnno = saldi.filter(s => s.referenceYear === annoCorrente);

        if (saldoAnno.length === 0) {
          Swal.fire('Saldo', `${nome} non ha saldi per l'anno corrente.`, 'info');
          return;
        }

        const righe = saldoAnno.map(s => `
                <tr>
                    <td style="padding: 8px 12px">${s.leaveType === 'VACATION' ? 'Ferie' : 'Permessi'}</td>
                    <td style="padding: 8px 12px">${Math.floor(s.totalQuantity)}</td>
                    <td style="padding: 8px 12px">${Math.floor(s.usedQuantity)}</td>
                    <td style="padding: 8px 12px"><strong>${Math.floor(s.remainingBalance)}</strong></td>
                </tr>
            `).join('');

        Swal.fire({
          title: `Saldo di ${nome}`,
          html: `
                    <table style="width:100%;border-collapse:collapse;text-align:left">
                        <thead>
                            <tr style="background:#f1f5f9;color:#64748b">
                                <th style="padding: 8px 12px">Tipo</th>
                                <th style="padding: 8px 12px">Totale</th>
                                <th style="padding: 8px 12px">Utilizzati</th>
                                <th style="padding: 8px 12px">Rimanenti</th>
                            </tr>
                        </thead>
                        <tbody>${righe}</tbody>
                    </table>
                `,
          confirmButtonText: 'Chiudi'
        });
      },
      error: err => {
        const msg = err.error?.message || err.error || 'Errore nel caricamento del saldo.';
        Swal.fire('Errore', msg, 'error');
      }
    });
  }

  getNomeMese(): string {
    const nome = this.meseCorrente.toLocaleDateString('it-IT', { month: 'long', year: 'numeric' });
    return nome.charAt(0).toUpperCase() + nome.slice(1);
  }

  generaCalendario() {
    const anno = this.meseCorrente.getFullYear();
    const mese = this.meseCorrente.getMonth();

    const primoGiorno = new Date(anno, mese, 1);
    const ultimoGiorno = new Date(anno, mese + 1, 0);

    let giornoInizioSettimana = primoGiorno.getDay() - 1;
    if (giornoInizioSettimana === -1) giornoInizioSettimana = 6;

    const giorni = [];

    for (let i = 0; i < giornoInizioSettimana; i++) {
      giorni.push({ data: null, assenze: [] });
    }

    for (let i = 1; i <= ultimoGiorno.getDate(); i++) {
      const dataCorrente = new Date(anno, mese, i);
      giorni.push({
        data: dataCorrente,
        assenze: this.calcolaAssenzePerGiorno(dataCorrente)
      });
    }

    this.giorniCalendario = giorni;
  }

  cambiaMese(offset: number) {
    const nuovaData = new Date(this.meseCorrente);
    nuovaData.setMonth(nuovaData.getMonth() + offset);
    this.meseCorrente = nuovaData;
    this.giornoSelezionato = null;
    this.generaCalendario();
    this.cdr.detectChanges();
  }

  calcolaAssenzePerGiorno(data: Date): any[] {
    const tutteRichieste = [...this.listaStorico, ...this.listaInAttesa];

    return tutteRichieste.filter(r => {
      if (r.leaveStatus !== 'APPROVED' && r.leaveStatus !== 'PENDING') return false;

      const [sd, sm, sy] = r.startDate.substring(0, 10).split('-').map(Number);
      const [ed, em, ey] = r.endDate.substring(0, 10).split('-').map(Number);

      const start = new Date(sy, sm - 1, sd, 0, 0, 0, 0);
      const end = new Date(ey, em - 1, ed, 23, 59, 59, 999);
      const dataCheck = new Date(data.getFullYear(), data.getMonth(), data.getDate(), 12, 0, 0);

      return dataCheck >= start && dataCheck <= end;
    });
  }

  isOggi(data: Date | null): boolean {
    if (!data) return false;
    const oggi = new Date();
    return data.getDate() === oggi.getDate() &&
      data.getMonth() === oggi.getMonth() &&
      data.getFullYear() === oggi.getFullYear();
  }

  getAssenzePerStato(giorno: any, stato: string): any[] {
    return giorno.assenze.filter((a: any) => a.leaveStatus === stato);
  }

  selezionaGiorno(giorno: any) {
    if (!giorno.data) return;
    if (this.giornoSelezionato === giorno) {
      this.giornoSelezionato = null;
      this.richiestaSelezionata = null;
    } else {
      this.giornoSelezionato = giorno;
      this.richiestaSelezionata = null;
    }
    this.cdr.detectChanges();
  }

  chiudiPannelloGiorno() {
    this.giornoSelezionato = null;
    this.richiestaSelezionata = null;
    this.cdr.detectChanges();
  }

  richiestaSelezionata: any = null;

  selezionaRichiesta(r: any) {
    this.richiestaSelezionata = this.richiestaSelezionata === r ? null : r;
    this.cdr.detectChanges();
  }
}

