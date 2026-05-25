import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'formatData',
    standalone: true
})
export class FormatDataPipe implements PipeTransform {
    private mesi = ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno',
        'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'];

    transform(dateStr: string): string {
        const parts = dateStr.substring(0, 10).split('-');
        const isISO = parts[0].length === 4;
        const giorno = parseInt(isISO ? parts[2] : parts[0]);
        const mese = this.mesi[parseInt(parts[1]) - 1];
        const anno = isISO ? parts[0] : parts[2];
        return `${giorno} ${mese} ${anno}`;
    }
}

