import '../globals.css'
import { useEffect, useState } from 'react'
import { Input } from '../components/ui/input'
import { Button } from '../components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '../components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select'
import { Pagination, PaginationContent, PaginationItem, PaginationPrevious, PaginationNext } from '@/components/ui/pagination'
import { format } from 'date-fns'
import { toast } from 'sonner'
import { api } from '../services/apiService'
import type { DateRange } from 'react-day-picker'
import DateRangePicker from '@/components/ui/date-range-picker'
import { Delete, Search } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { MainLayout } from '@/components/layout/MainLayout'
import dayjs from '@/lib/dayjs'

export function Checklists() {
  const [checklists, setChecklists] = useState<any[]>([])
  const [selectedChecklist, setSelectedChecklist] = useState<any | null>(null)
  const [filterField, setFilterField] = useState('bench')
  const [filterText, setFilterText] = useState('')
  const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined)
  const [loading, setLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)

  const itemsPerPage = 6

  const totalPages = Math.ceil(checklists.length / itemsPerPage)
  const paginatedItems = checklists.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )

  useEffect(() => {
    loadChecklists()
  }, [])

  async function loadChecklists(params = {}) {
    try {
      setLoading(true)
      const response = await api.get('/checklists/filters', { params })
      const sorted = response.data.sort((a, b) => a.id - b.id)
      setChecklists(sorted)
      setCurrentPage(1)
    } catch {
      toast.error('Erro ao carregar checklists')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: number) {
    const confirmed = window.confirm('Tem certeza que deseja excluir essa checklist?')
    if (!confirmed) return

    setLoading(true)
    const toastId = toast.loading('Excluindo checklist...')

    try {
      await api.delete(`/checklists/${id}`)
      toast.dismiss(toastId)
      toast.success('Checklist excluído com sucesso!')
      loadChecklists()
    } catch {
      toast.dismiss(toastId)
      toast.error('Erro ao excluir checklist.')
    } finally {
      setLoading(false)
    }
  }

  async function handleFilterSubmit(e: React.FormEvent) {
    e.preventDefault()
    
  console.log('filterField:', filterField)
  console.log('filterText:', filterText)
  console.log('dateRange:', dateRange)
    const params: Record<string, string> = {}

    if (filterText) {
      params[filterField] = filterText
    }

    if (dateRange?.from) {
      const start = new Date(dateRange.from)
      start.setHours(0, 0, 0, 0)
      params.startDate = start.toISOString()
    }

    if (dateRange?.to) {
      const end = new Date(dateRange.to)
      end.setHours(23, 59, 59, 999)
      params.endDate = end.toISOString()
    }
    console.log('Filtro enviado:', params)
    loadChecklists(params)
  }

  return (
    <MainLayout>
    <div className="p-6 max-w-4xl mx-auto space-y-4">
      <h1 className="text-3xl font-bold">Checklists</h1>
      <Card>
        <CardContent>
          <CardHeader>
      <form onSubmit={handleFilterSubmit} className="flex items-center gap-2" >
        <Select value={filterField} onValueChange={setFilterField}>
          <SelectTrigger className="w-[140px]">
            <SelectValue placeholder="Filtrar por"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="bench">Bancada</SelectItem>
            <SelectItem value="shift">Turno</SelectItem>
            <SelectItem value="user">Operador</SelectItem>
          </SelectContent>
        </Select>

        <Input
          placeholder="Digite o valor"
          className="w-auto"
          value={filterText}
          onChange={(e) => setFilterText(e.target.value)}
        />

        <DateRangePicker value={dateRange} onRangeChange={setDateRange}/>
        <Button type="submit" variant="link">
        <Search className='w-4 h-4 mr-2'/>
        Filtrar
        </Button>
      </form>
      </CardHeader>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Data</TableHead>
              <TableHead>Turno</TableHead>
              <TableHead>Operador</TableHead>
              <TableHead>Bancada</TableHead>
              <TableHead>Ações</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {checklists.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={6} className="text-center">
                  Nenhum checklist encontrado.
                </TableCell>
              </TableRow>
            )}

            {paginatedItems.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.id}</TableCell>
                <TableCell>
                  {dayjs.utc(item.dateTime).format('DD/MM/YYYY HH:mm')}
                </TableCell>
                <TableCell>{item.shift}</TableCell>
                <TableCell>{item.user}</TableCell>
                <TableCell>{item.bench}</TableCell>
                <TableCell className="space-x-2">
                  <Dialog>
                    <DialogTrigger asChild>
                      <Button
                        variant="outline"
                        onClick={() => setSelectedChecklist(item)}
                      >
                        Ver detalhes
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Detalhes do Checklist</DialogTitle>
                        <DialogDescription>
                          {item.user} -{' '}
                          {format(new Date(item.dateTime), 'dd/MM/yyyy HH:mm')}
                        </DialogDescription>
                      </DialogHeader>
                      <div className="space-y-2 max-h-64 overflow-y-auto">
                        {Object.entries(item.checklistItems).map(
                          ([key, val]: any) => (
                            <div key={key} className="flex justify-between">
                              <span className="font-medium">{key}</span>
                              <span>{val.response}</span>
                            </div>
                          )
                        )}
                      </div>
                    </DialogContent>
                  </Dialog>
                  <Button
                    className='w-4 h-4 relative left-11 items-center justify-center p-0'
                    variant="ghost"
                    onClick={() => handleDelete(item.id)}
                  >
                    <Delete />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <CardFooter className="flex justify-center">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>

              {Array.from({ length: totalPages }, (_, i) => (
                <PaginationItem key={i}>
                  <button
                    onClick={() => setCurrentPage(i + 1)}
                    className={`px-3 py-1 rounded-md text-sm ${
                      currentPage === i + 1
                        ? 'bg-zinc-800 text-white'
                        : 'hover:bg-zinc-200'
                    }`}
                  >
                    {i + 1}
                  </button>
                </PaginationItem>
              ))}
              <PaginationItem>
                <PaginationNext
                  onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                  className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </CardFooter>
        </CardContent>
      </Card>
    </div>
    </MainLayout>
  )
}