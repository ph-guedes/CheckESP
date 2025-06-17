import { useEffect, useState } from 'react'
import { api } from '@/services/apiService'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { BarChartIcon } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { MainLayout } from '@/components/layout/MainLayout'
import { type ChartConfig, ChartContainer, ChartLegend, ChartLegendContent, ChartTooltip, ChartTooltipContent, } from "@/components/ui/chart"
import { BarChart, CartesianGrid, XAxis, YAxis, Bar, } from "recharts";
import dayjs from '@/lib/dayjs'
import { Link } from 'react-router-dom'

export default function Home() {
  const chartConfig: ChartConfig = {quantidade: { label: "Quantidade", color: "#4f46e5" } , OK: { label: "OK", color: "#16a34a" }, NOK: { label: "NOK", color: "#dc2626" }, }
  const [totalChecklists, setTotalChecklists] = useState(0)
  const [totalUsers, setTotalUsers] = useState(0)
  const [todayChecklists, setTodayChecklists] = useState(0)
  const [topNok, setTopNok] = useState<Record<string, number>>({});
  const [lastChecklist, setLastChecklist] = useState<any>(null)
  const [data, setData] = useState<{ modelo: string; quantidade: number }[]>([]);
  const [itemsData, setItemsData] = useState<{ item: string; NOK: number; OK: number;} []>([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [usersRes, totalRes, todayRes, topRes, lastRes, models, itemStats] = await Promise.all([
          api.get('/users/count'),
          api.get('/checklists/count'),
          api.get('/checklists/count/today'),
          api.get('/checklists/top-nok'),
          api.get('/checklists/last'),
          api.get('/checklists/stats/models'),
          api.get('/checklists/stats/items-status')
        ])

        setTotalUsers(usersRes.data)
        setTotalChecklists(totalRes.data)
        setTodayChecklists(todayRes.data)
        setTopNok(topRes.data)
        setLastChecklist(lastRes.data)
        setData(models.data)
        setItemsData(itemStats.data)
      } catch (error) {
        console.error('Erro ao carregar dados da dashboard:', error)
      }
    }

    fetchData()
  }, [])

  return (
    <MainLayout>
      <div className="p-6 space-y-6 max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold">Dashboard</h1>

        <div className="grid grid-cols-2 md:grid-cols-3 gap-8">
          <Card>
            <CardHeader>
              <CardTitle>Usuários</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{totalUsers}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Total de Checklists</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{totalChecklists}</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Checklists Hoje</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{todayChecklists}</p>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card>
            <CardHeader className="flex items-center justify-between">
              <CardTitle>Modelos mais verificados</CardTitle>
              <BarChartIcon className="w-5 h-5 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {data.length === 0 ? (
                <div className="h-48 flex items-center justify-center text-muted-foreground">
                  Carregando...
                </div>
              ) : (
                <ChartContainer config={chartConfig} className="min-h-[200px] w-full">
                  <BarChart data={data}>
                    <CartesianGrid vertical={false} />
                    <XAxis
                      dataKey="modelo"
                      tickLine={false}
                      tickMargin={10}
                      axisLine={false}
                    />
                    <YAxis allowDecimals={false} />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <ChartLegend content={<ChartLegendContent />} />
                    <Bar dataKey="quantidade" fill="var(--color-quantidade)" radius={4} />
                  </BarChart>
                </ChartContainer>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex items-center justify-between">
              <CardTitle>Status por Item</CardTitle>
              <BarChartIcon className="w-5 h-5 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {itemsData.length === 0 ? (
                <div className="h-48 flex items-center justify-center text-muted-foreground">
                  Carregando...
                </div>
              ) : (
                <ChartContainer config={chartConfig} className="min-h-[200px] w-full">
                  <BarChart data={itemsData}>
                    <CartesianGrid vertical={false} />
                    <XAxis
                      dataKey="item"
                      tickLine={false}
                      tickMargin={10}
                      axisLine={false}
                      tickFormatter={(value) => value.length > 3 ? value.slice(0, 3) + "…" : value}
                    />
                    <ChartTooltip content={<ChartTooltipContent />} />
                    <ChartLegend content={<ChartLegendContent />} />
                    <Bar dataKey="OK" fill="var(--color-OK)" radius={4} />
                    <Bar dataKey="NOK" fill="var(--color-NOK)" radius={4} />
                  </BarChart>
                </ChartContainer>
              )}
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className='text-lg'>Itens mais problemáticos</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-sm">
                {Object.entries(topNok).length === 0 && (
                  <li className="text-muted-foreground">Carregando...</li>
                )}
                {Object.entries(topNok).map(([item, count]) => (
                  <li key={item}>
                    {item} — <Badge variant="destructive">{count} NOK{count > 1 ? "'s" : ""}</Badge>
                  </li>
                ))}
              </ul>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className='text-lg'>Última Checklist</CardTitle>
            </CardHeader>
            <CardContent>
              <ul className="space-y-2 text-sm">
              {lastChecklist ? (
                <li>
                  {lastChecklist.user}  —  {dayjs.utc(lastChecklist.dateTime)
                  .format('DD/MM/YYYY - HH:mm')}
                </li>
              ) : (
                <li className="text-sm text-muted-foreground">Nenhuma</li>
              )}
              </ul>
            </CardContent>
          </Card>
        </div>

        <div className="flex flex-wrap gap-4">
          <Button asChild>
            <Link to="/items">Novo Item</Link>
          </Button>
          <Button variant="outline" asChild>
            <Link to="/uids">Gerenciar UIDs</Link>
          </Button>
          <Button variant="outline">Exportar Dados</Button>
        </div>
      </div>
    </MainLayout>
  )
}
