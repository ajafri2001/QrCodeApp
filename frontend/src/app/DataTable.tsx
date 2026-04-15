'use client'

import { useState } from 'react'

import type { ColumnDef, ColumnFiltersState, SortingState, VisibilityState } from '@tanstack/react-table'
import {
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable
} from '@tanstack/react-table'

import { Checkbox } from '@/components/ui/checkbox'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'

const data: History[] = [
    {
        date: new Date('2026-01-01'),
        originalUrl: 'https://example.com/1',
        mimetype: 'PNG',
        downloadQr: '/qr/1.png'
    },
    {
        date: new Date('2026-02-15'),
        originalUrl: 'https://example.com/2',
        mimetype: 'SVG',
        downloadQr: '/qr/2.svg'
    },
    {
        date: new Date('2026-03-10'),
        originalUrl: 'https://example.com/3',
        mimetype: 'PNG',
        downloadQr: '/qr/3.png'
    }
]

export type History = {
    date: Date
    originalUrl: string
    mimetype: "SVG" | "PNG",
    downloadQr: string
}

export const columns: ColumnDef<History>[] = [
    {
        id: 'select',
        header: ({ table }) => (
            <Checkbox
                checked={
                    table.getIsAllPageRowsSelected() ||
                    (table.getIsSomePageRowsSelected() && 'indeterminate')
                }
                onCheckedChange={value => table.toggleAllPageRowsSelected(!!value)}
                aria-label='Select all'
            />
        ),
        cell: ({ row }) => (
            <Checkbox
                checked={row.getIsSelected()}
                onCheckedChange={value => row.toggleSelected(!!value)}
                aria-label='Select row'
            />
        ),
        enableSorting: false,
        enableHiding: false
    },
    {
        accessorKey: 'date',
        header: 'Date',
        enableSorting: true,
        cell: ({ row }) => {
            const date = row.getValue<Date>('date')
            return <div>{date.toLocaleDateString()}</div>
        }
    },
    {
        accessorKey: 'originalUrl',
        header: 'Original URL',
        cell: ({ row }) => {
            const url = row.getValue<string>('originalUrl')
            return (
                <a href={url} target='_blank' className='text-blue-500 underline'>
                    {url}
                </a>
            )
        }
    },
    {
        accessorKey: 'mimetype',
        header: 'Type',
        cell: ({ row }) => {
            const type = row.getValue<'svg' | 'png'>('mimetype')

            return (
                <span
                    className={`px-2 py-1 rounded text-xs font-medium ${type === 'svg'
                        ? 'bg-purple-100 text-purple-700'
                        : 'bg-green-100 text-green-700'
                        }`}
                >
                    {type.toUpperCase()}
                </span>
            )
        }
    },
    {
        accessorKey: 'downloadQr',
        header: 'QR Code',
        cell: ({ row }) => {
            const qr = row.getValue<string>('downloadQr')
            return (
                <a href={qr} download className='text-blue-500 underline'>
                    Download
                </a>
            )
        }
    }
]

const DataTable = () => {
    const [sorting, setSorting] = useState<SortingState>([
        { id: 'date', desc: true }
    ])
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([])
    const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = useState({})

    const table = useReactTable({
        data,
        columns,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection
        }
    })

    return (
        <div className='w-full'>
            <div className='rounded-md border'>
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map(headerGroup => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map(header => {
                                    return (
                                        <TableHead key={header.id}>
                                            {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                                        </TableHead>
                                    )
                                })}
                            </TableRow>
                        ))}
                    </TableHeader>
                    <TableBody>
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map(row => (
                                <TableRow key={row.id} data-state={row.getIsSelected() && 'selected'}>
                                    {row.getVisibleCells().map(cell => (
                                        <TableCell key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={columns.length} className='h-24 text-center'>
                                    No results.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    )
}

export default DataTable
