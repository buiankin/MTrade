package ru.code22.mtrade

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import ru.code22.mtrade.MTradeContentProvider.Companion.AGENTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.AGENTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS30_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS30_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS30_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.AGREEMENTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CASH_PAYMENTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CASH_PAYMENTS_JOURNAL_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CASH_PAYMENTS_JOURNAL_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CASH_PAYMENTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CLIENTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CLIENTS_PRICE_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CLIENTS_PRICE_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CLIENTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CLIENTS_WITH_SALDO_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CURATORS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CURATORS_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CURATORS_PRICE_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.CURATORS_PRICE_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.CURATORS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DB_NAME
import ru.code22.mtrade.MTradeContentProvider.Companion.DB_VERSION
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_CONTRACTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_CONTRACTS_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_CONTRACTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_LINES_COMPLEMENTED_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_LINES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_LINES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTRIBS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTR_POINTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTR_POINTS_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.DISTR_POINTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.EQUIPMENT_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.EQUIPMENT_RESTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.EQUIPMENT_RESTS_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.EQUIPMENT_RESTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.EQUIPMENT_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.GPS_COORD_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.GPS_COORD_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_ORDER_COLOR_FOR_TRIGGER
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_REFUND_COLOR_FOR_TRIGGER
import ru.code22.mtrade.MTradeContentProvider.Companion.JOURNAL_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.LOG_TAG
import ru.code22.mtrade.MTradeContentProvider.Companion.MESSAGES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.MESSAGES_LIST_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.MESSAGES_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.MESSAGES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.MTRADE_LOG_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.MTRADE_LOG_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_HIERARCHY_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_HIERARCHY_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_SURFING_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.NOMENCLATURE_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.OCCUPIED_PLACES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.OCCUPIED_PLACES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_JOURNAL_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_JOURNAL_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_LINES_COMPLEMENTED_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_LINES_COMPLEMENTED_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_LINES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_LINES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_PLACES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_PLACES_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_PLACES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORDERS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ORGANIZATIONS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ORGANIZATIONS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.PERMISSIONS_REQUESTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.PERMISSIONS_REQUESTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.PLACES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.PLACES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICES_AGREEMENTS30_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICES_AGREEMENTS30_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICETYPES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.PRICETYPES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.REAL_ROUTES_DATES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.REAL_ROUTES_DATES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.REAL_ROUTES_LINES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.REAL_ROUTES_LINES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_LINES_COMPLEMENTED_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_LINES_COMPLEMENTED_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_LINES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_LINES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.REFUNDS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.RESTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.RESTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_DATES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_DATES_LIST_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_DATES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_LINES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_LINES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.ROUTES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALDO_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.SALDO_EXTENDED_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.SALDO_EXTENDED_JOURNAL_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALDO_EXTENDED_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALDO_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALES_L2_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALES_LOADED_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.SALES_LOADED_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SALES_L_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SEANCES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SETTINGS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.SIMPLE_DISCOUNTS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.SIMPLE_DISCOUNTS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.STOCKS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.STOCKS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGENTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGENTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS30
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS30_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS30_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_AGREEMENTS_WITH_SALDO_ONLY_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CASH_PAYMENTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CASH_PAYMENTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CASH_PAYMENTS_JOURNAL
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CLIENTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CLIENTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CLIENTS_PRICE
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CLIENTS_WITH_SALDO
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CREATE_SALES_L
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CREATE_VIEWS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CURATORS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CURATORS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CURATORS_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_CURATORS_PRICE
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISCOUNTS_STUFF_MEGA
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISCOUNTS_STUFF_OTHER
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISCOUNTS_STUFF_SIMPLE
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS_CONTRACTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS_CONTRACTS_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS_LINES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTRIBS_LINES_COMPLEMENTED
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTR_POINTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTR_POINTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_DISTR_POINTS_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_EQUIPMENT
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_EQUIPMENT_RESTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_EQUIPMENT_RESTS_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_GPS_COORD
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_JOURNAL
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_JOURNAL_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_MESSAGES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_MESSAGES_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_MESSAGES_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_MTRADE_LOG
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_NOMENCLATURE
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_NOMENCLATURE_HIERARCHY
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_NOMENCLATURE_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_NOMENCLATURE_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_NOMENCLATURE_SURFING
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_OCCUPIED_PLACES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_JOURNAL
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_LINES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_LINES_COMPLEMENTED
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_PLACES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_PLACES_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_SILENT
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORDERS_SILENT_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORGANIZATIONS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ORGANIZATIONS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PERMISSIONS_REQUESTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PLACES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PRICES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PRICESV_MEGA
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PRICESV_OTHER
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PRICES_AGREEMENTS30
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_PRICETYPES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REAL_ROUTES_DATES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REAL_ROUTES_DATES_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REAL_ROUTES_LINES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS_LINES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS_LINES_COMPLEMENTED
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS_SILENT
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REFUNDS_SILENT_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_REINDEX
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_RESTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_RESTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_RESTS_SALES_STUFF
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES_DATES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES_DATES_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES_DATES_LIST
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES_DATES_LIST_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_ROUTES_LINES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALDO
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALDO_EXTENDED
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALDO_EXTENDED_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALDO_EXTENDED_JOURNAL
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALES_L2
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SALES_LOADED
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SEANCES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SEANCES_INCOMING
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SEANCES_OUTGOING
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SETTINGS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SIMPLE_DISCOUNTS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SIMPLE_DISCOUNTS_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_SORT
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_STOCKS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_VACUUM
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_VERSIONS
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_VERSIONS_SALES
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_VICARIOUS_POWER
import ru.code22.mtrade.MTradeContentProvider.Companion.URI_VICARIOUS_POWER_ID
import ru.code22.mtrade.MTradeContentProvider.Companion.VERSIONS_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.VERSIONS_SALES_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.VERSIONS_SALES_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.VERSIONS_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.VICARIOUS_POWER_CONTENT_URI
import ru.code22.mtrade.MTradeContentProvider.Companion.VICARIOUS_POWER_TABLE
import ru.code22.mtrade.MTradeContentProvider.Companion.agentsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreements30ListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreements30ListWithSaldoOnlyProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreements30ProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreementsListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreementsListWithSaldoOnlyProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.agreementsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.cashPaymentsJournalProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.cashPaymentsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.clientsPriceProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.clientsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.clientsWithSaldoProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.curatorsListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.curatorsPriceProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.curatorsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distrPointsListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distrPointsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distribsContractsListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distribsContractsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distribsLinesComplementedProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distribsLinesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.distribsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.equipmentProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.equipmentRestsListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.equipmentRestsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.gpsCoordProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.journalProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.messagesListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.messagesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.mtradeLogProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.nomenclatureHierarchyProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.nomenclatureListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.nomenclatureProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.nomenclatureSurfingProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.occupiedPlacesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersJournalProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersLinesComplementedProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersLinesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersPlacesListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersPlacesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.ordersProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.organizationsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.permissionsRequestsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.placesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.pricesAgreements30ProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.pricesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.pricetypesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.realRoutesDatesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.realRoutesLinesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.refundsLinesComplementedProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.refundsLinesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.refundsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.restsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.routesDatesListProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.routesDatesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.routesLinesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.routesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.sUriMatcher
import ru.code22.mtrade.MTradeContentProvider.Companion.saldoExtendedJournalProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.saldoExtendedProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.saldoProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.salesL2ProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.salesLoadedProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.seancesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.settingsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.simpleDiscountsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.stocksProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.versionsProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.versionsSalesProjectionMap
import ru.code22.mtrade.MTradeContentProvider.Companion.vicariousPowerProjectionMap
import kotlin.text.toInt

class MTradeDBHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA encoding = \"UTF-8\"")
        // insert into xxx
        // select * from yyyy;
        // create table xxx
        // as select * from yyy
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(LOG_TAG, " --- onCreate database --- ")
        db.execSQL("PRAGMA encoding = \"UTF-8\"")


        /*
            try {
                int insertCount = insertFromFile(getContext(), db, R.raw.backup_dch);
                //Toast.makeText(this, "Rows loaded from file= " + insertCount, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                //Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
             */
        db.execSQL(
            "create table messages (" +
                    "_id integer primary key autoincrement," +
                    "uid text," +
                    "sender_id text," +
                    "receiver_id text," +
                    "text text," +
                    "fname text," +
                    "datetime text," +
                    "acknowledged int," +
                    "ver int," +  // новые
                    "type_idx int," +
                    "date1 text," +
                    "date2 text," +
                    "client_id text," +
                    "agreement_id text," +
                    "nomenclature_id text," +
                    "report text," +  //
                    "isMark int default 0," +
                    "isMarkCnt int default 0," +  //
                    "UNIQUE('uid')" +
                    ");"
        )

        db.execSQL(
            "create table seances (" +
                    "incoming text," +
                    "outgoing text" +
                    ");"
        )

        db.execSQL("insert into seances(incoming) values ('CREATED')")

        db.execSQL(
            "create table versions (" +
                    "param text," +
                    "ver int," +
                    "UNIQUE ('param')" +
                    ");"
        )

        db.execSQL(
            "create table sales_versions (" +
                    "param text," +
                    "ver int," +
                    "UNIQUE ('param')" +
                    ");"
        )

        db.execSQL(
            "create table clients (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "isFolder int," +
                    "parent_id text," +
                    "parent_id_0 text," +
                    "parent_id_1 text," +
                    "parent_id_2 text," +
                    "parent_id_3 text," +
                    "parent_id_4 text," +
                    "code text," +
                    "descr text," +
                    "descr_lower text," +
                    "descrFull text," +
                    "address text," +
                    "address2 text," +
                    "comment text," +
                    "curator_id text," +
                    "priceType text," +
                    "blocked int," +
                    "flags int," +
                    "card_num text," +
                    "phone_num text," +
                    "email_for_cheques text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table organizations (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table agreements (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "owner_id text," +
                    "organization_id text," +
                    "code text," +
                    "descr text," +
                    "price_type_id text," +
                    "default_manager_id text," +
                    "sale_id text," +
                    "kredit_days int," +
                    "kredit_sum double," +
                    "flags int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table agreements30 (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "owner_id text," +
                    "organization_id text," +
                    "code text," +
                    "descr text," +
                    "price_type_id text," +
                    "default_manager_id text," +
                    "default_stock_id text," +
                    "sale_id text," +
                    "kredit_days int," +
                    "kredit_sum double," +
                    "flags int," +
                    "UNIQUE ('id', 'owner_id')" +
                    ");"
        )

        db.execSQL(
            "create table prices_agreements30 (" +
                    "_id integer primary key autoincrement, " +
                    "agreement30_id text," +
                    "nomenclature_id text," +
                    "pack_id text," +  // Пока не используется
                    "ed_izm_id text," +
                    "edIzm text," +
                    "price double," +
                    "k double," +
                    "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                    ");"
        )


        db.execSQL(
            "create table pricetypes (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "isFolder text," +  // это баг, тип строка а не число, да и parent_id в справочнике нет
                    "code text," +
                    "descr text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table clients_price (" +
                    "_id integer primary key autoincrement, " +
                    "client_id text," +
                    "nomenclature_id text," +
                    "priceProcent double," +
                    "priceAdd double," +
                    "UNIQUE ('client_id', 'nomenclature_id')" +
                    ");"
        )

        db.execSQL(
            "create table curators_price (" +
                    "_id integer primary key autoincrement, " +
                    "curator_id text," +
                    "nomenclature_id text," +
                    "priceProcent double," +
                    "priceAdd double," +
                    "UNIQUE ('curator_id', 'nomenclature_id')" +
                    ");"
        )

        db.execSQL(
            "create table simple_discounts (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "priceProcent double," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table stocks (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "isFolder text," +
                    "code text," +
                    "descr text," +
                    "flags int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Nomenclature (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id TEXT," +
                    "isFolder INT," +
                    "parent_id text," +
                    "parent_id_0 text," +
                    "parent_id_1 text," +
                    "parent_id_2 text," +
                    "parent_id_3 text," +
                    "parent_id_4 text," +
                    "code TEXT," +
                    "descr TEXT," +
                    "descr_lower text," +
                    "descrFull TEXT," +
                    "quant_1 TEXT," +
                    "quant_2 TEXT," +
                    "edizm_1_id TEXT," +
                    "edizm_2_id TEXT," +
                    "quant_k_1 DOUBLE," +
                    "quant_k_2 DOUBLE," +
                    "opt_price DOUBLE," +
                    "m_opt_price DOUBLE," +
                    "rozn_price DOUBLE," +
                    "incom_price DOUBLE," +
                    "IsInPrice INT," +
                    "flagWithoutDiscont INT," +
                    "weight_k_1 DOUBLE," +
                    "weight_k_2 DOUBLE," +
                    "min_quantity DOUBLE," +  // минимальное продаваемое количество за 1 раз
                    "multiplicity DOUBLE," +  // кратность количества (нельзя дробить)
                    "required_sales DOUBLE," +  // план продажи за месяц по каждому клиенту
                    "flags INT," +
                    "image_file text," +
                    "image_file_checksum INT," +  // не используется, т.к. для расчета требуется считывать файл
                    "order_for_sorting INT," +
                    "group_of_analogs INT," +
                    "nomenclature_color int default 0," +
                    "backorder int default 0," +
                    "image_width INT," +
                    "image_height INT," +
                    "image_file_size INT," +
                    "compose_with text," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Rests (" +
                    "stock_id TEXT," +
                    "nomenclature_id TEXT," +
                    "organization_id TEXT default ''," +
                    "quantity DOUBLE," +
                    "quantity_reserve DOUBLE," +
                    "isUsed int default 0," +
                    "UNIQUE ('stock_id', 'nomenclature_id','organization_id')" +
                    ");"
        )

        db.execSQL(
            "create table prices (" +
                    "_id integer primary key autoincrement, " +
                    "nomenclature_id text," +
                    "price_type_id text," +
                    "ed_izm_id text," +
                    "edIzm text," +
                    "price double," +
                    "priceProcent double," +  // скорее всего это лишнее поле
                    "k double," +
                    "isUsed int default 0," +
                    "UNIQUE ('nomenclature_id','price_type_id')" +
                    ");"
        )


        db.execSQL(
            "create table agents (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )


        db.execSQL(
            "create table curators (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "parent_id text," +
                    "isFolder int," +
                    "code text," +
                    "descr text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table distr_points (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "owner_id text," +
                    "code text," +
                    "descr text," +
                    "address text," +
                    "phones text," +
                    "contacts text," +
                    "price_type_id text," +
                    "UNIQUE (id, owner_id)" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE Saldo (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client_id TEXT," +
                    "saldo DOUBLE," +
                    "saldo_past DOUBLE," +
                    "saldo_past30 DOUBLE default 0," +
                    "isUsed int default 0," +
                    "UNIQUE ('client_id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE saldo_extended (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client_id TEXT," +
                    "agreement30_id TEXT," +
                    "agreement_id TEXT," +
                    "document_id TEXT," +
                    "manager_id TEXT," +
                    "document_descr TEXT," +
                    "document_datetime TEXT," +
                    "manager_descr TEXT," +
                    "agreement_descr TEXT," +
                    "organization_descr TEXT," +
                    "saldo DOUBLE," +
                    "saldo_past DOUBLE," +
                    "isUsed int default 0," +
                    "UNIQUE ('client_id','agreement30_id','agreement_id','document_id','manager_id')" +
                    ");"
        )

        db.execSQL(
            "create table ordersLines (" +
                    "_id integer primary key autoincrement, " +
                    "order_id integer," +  // внутренний код
                    "nomenclature_id text," +
                    "client_id text," +
                    "quantity_requested double," +
                    "quantity double," +
                    "price double," +
                    "total double," +
                    "discount DOUBLE," +
                    "k double," +
                    "ed text," +
                    "shipping_time text," +
                    "comment_in_line text," +  // 0-строка не менялась
                    // 1-добавленные строки
                    // 2-строка удалена
                    // 3-строка изменена
                    //"editing_state int," +
                    // для состояния 3 - _id измененной строки
                    //"old_id int" +
                    "lineno int" +
                    ");"
        )

        db.execSQL(
            "create table orders (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +
                    "version int," +
                    "version_ack int," +
                    "versionPDA int," +
                    "versionPDA_ack int," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "client_id text," +
                    "agreement30_id text," +
                    "agreement_id text," +
                    "distr_point_id text," +
                    "comment text," +
                    "comment_closing text," +
                    "comment_payment text," +
                    "closed_not_full int," +
                    "state int," +
                    "curator_id text," +
                    "bw int," +
                    "trade_type text," +
                    "datecreation text," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpsaccuracy float," +
                    "gpstype int," +
                    "accept_coord int," +
                    "dont_need_send int," +
                    "price_type_id text," +
                    "stock_id text," +
                    "sum_doc double," +
                    "weight_doc double," +
                    "shipping_type int," +
                    "shipping_time int," +
                    "shipping_begin_time text," +
                    "shipping_end_time text," +
                    "shipping_date text," +
                    "simple_discount_id text," +
                    "create_client int," +
                    "create_client_descr text," +
                    "create_client_surname text," +
                    "create_client_firstname text," +
                    "create_client_lastname text," +
                    "create_client_phone text," +
                    "place_num int," +
                    "card_num int," +
                    "pay_credit double," +
                    "ticket_m double," +
                    "ticket_w double," +
                    "quant_m int," +
                    "quant_w int," +
                    "quant_mw int," +
                    "manager_comment text," +
                    "theme_comment text," +
                    "phone_num text," +
                    "sum_shipping double," +  // 0-документ в нормальном состоянии
                    // 1-новый документ, записать не успели
                    // 2-документ начали редактировать, но не записали и не отменили изменения
                    "editing_backup int," +  // для состояния 2 - _id редактируемого документа
                    // в других случаях - не важно
                    "old_id int" +  // До 19.02.2018 editing_backup тут не было
                    //"UNIQUE ('uid','id')" +
                    ");"
        )
        // С 19.02.2018
        db.execSQL("CREATE UNIQUE INDEX OrdersUniqueIndex ON orders ('uid','id','editing_backup')")

        db.execSQL(
            "create table ordersPlaces (" +
                    "_id integer primary key autoincrement, " +
                    "order_id integer," +  // внутренний код
                    "place_id text" +
                    ");"
        )

        db.execSQL(
            "create table cash_payments (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +
                    "version int," +
                    "version_ack int," +
                    "versionPDA int," +
                    "versionPDA_ack int," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "client_id text," +
                    "agreement_id text," +
                    "comment text," +
                    "comment_closing text," +
                    "state int," +
                    "curator_id text," +
                    "manager_descr text," +
                    "organization_descr text," +
                    "sum_doc double," +
                    "vicarious_power_id text, " +
                    "vicarious_power_descr text, " +
                    "datecreation text," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpsaccuracy float," +
                    "gpstype int," +
                    "accept_coord int," +
                    "distr_point_id text," +  // это только для того, чтобы документ привязался к маршруту
                    "UNIQUE ('uid','id')" +
                    ");"
        )


        db.execSQL(
            "create table vicarious_power (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "descr text," +
                    "numdoc text," +
                    "datedoc text," +
                    "date_action text," +
                    "comment text," +
                    "client_id text," +
                    "client_descr text," +
                    "agreement_id text," +
                    "agreement_descr text," +
                    "fio_descr text," +
                    "manager_id text," +
                    "manager_descr text," +
                    "organization_id text," +
                    "organization_descr text," +
                    "state text," +
                    "sum_doc double," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table refundsLines (" +
                    "_id integer primary key autoincrement, " +
                    "refund_id integer," +  // внутренний код
                    "nomenclature_id text," +
                    "client_id text," +
                    "quantity_requested double," +
                    "quantity double," +  //"price double," +
                    //"total double," +
                    //"discount DOUBLE,"+
                    "k double," +
                    "ed text," +
                    "comment_in_line text" +
                    ");"
        )

        db.execSQL(
            "create table refunds (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +
                    "version int," +
                    "version_ack int," +
                    "versionPDA int," +
                    "versionPDA_ack int," +
                    "base_doc_id text," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "client_id text," +
                    "agreement_id text," +
                    "distr_point_id text," +
                    "comment text," +
                    "comment_closing text," +
                    "closed_not_full int," +
                    "state int," +
                    "curator_id text," +
                    "bw int," +
                    "trade_type text," +
                    "datecreation text," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate float," +
                    "gpsaccuracy float," +
                    "accept_coord int," +
                    "dont_need_send int," +
                    "price_type_id text," +
                    "stock_id text," +
                    "sum_doc double," +
                    "weight_doc double," +
                    "simple_discount_id text," +  // 20.05.2020
                    "shipping_type int," +  //
                    "UNIQUE ('uid','id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE distribsContracts (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id TEXT," +
                    "position int," +
                    "code TEXT," +
                    "descr TEXT," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table distribsLines (" +
                    "_id integer primary key autoincrement, " +
                    "distribs_id integer," +  // внутренний код
                    "contract_id text," +
                    "quantity double" +
                    ");"
        )

        db.execSQL(
            "create table distribs (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +
                    "version int," +
                    "version_ack int," +
                    "versionPDA int," +
                    "versionPDA_ack int," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "client_id text," +
                    "curator_id text," +
                    "distr_point_id text," +
                    "state int," +
                    "comment text," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpsaccuracy float," +
                    "gpstype int," +
                    "accept_coord int," +
                    "UNIQUE ('uid','id')" +
                    ");"
        )


        /*
          db.execSQL("create table debt_ext (" +
                  "_id integer primary key autoincrement, " +
                  "client_id text," +
                  "agreement_id text," +
                  "curator_id text," +
                  "document text," +
                  "plan_pay_date text," +
                  "isExpanded int,"+ // 0 - если общий долг, 1,2,3 и т.д - детальные записи
                  "sum_debt double," +
                  "UNIQUE ('client_id','agreement_id','curator_id','isExpanded')" +
                  ");");
         */

        /*
          cv.clear();
          cv.put("numdoc", "0001");
          cv.put("datedoc", "20130101");
          db.insert("orders", null, cv);

          cv.clear();
          cv.put("descr", "Иванов И.И.");
          cv.put("address", "Нулевой километр");
          db.insert("clients", null, cv);

          cv.clear();
          cv.put("descr", "Петров П.П.");
          cv.put("address", "Адрес неизвестен");
          db.insert("clients", null, cv);
          */
        db.execSQL(
            "create table settings (" +
                    "_id integer primary key autoincrement, " +
                    "fmt text, " +
                    "ticket_m double," +
                    "ticket_w double," +
                    "agent_id text," +
                    "gps_interval ind default 0," +
                    "agent_price_type_id text" +
                    ");"
        )

        db.execSQL(
            "create table salesloaded (" +
                    "_id integer primary key autoincrement, " +
                    "ver int," +
                    "datedoc text," +
                    "numdoc text," +
                    "refdoc text," +
                    "client_id text," +
                    "curator_id text," +
                    "distr_point_id text," +
                    "nomenclature_id text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double," +
                    "UNIQUE ('nomenclature_id', 'refdoc')" +
                    ");"
        )

        db.execSQL(
            "create table nomenclature_hierarchy (" +
                    "id text," +
                    "ord_idx int," +
                    "groupDescr text," +
                    "level int," +
                    "level0_id text," +
                    "level1_id text," +
                    "level2_id text," +
                    "level3_id text," +
                    "level4_id text," +
                    "level5_id text," +
                    "level6_id text," +
                    "level7_id text," +
                    "level8_id text," +
                    "dont_use_in_hierarchy int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table salesL (" +
                    "nomenclature_id text," +
                    "client_id text," +
                    "distr_point_id text," +  // данные, загруженные из 1С (предыдущие периоды)
                    "strQuantity text," +  // продажи по датам N+N+N+N+
                    "quantity double," +  // общее количество продаж
                    // данные в телефоне (текущий период, обновляются при записи или удалении заказа, отбор по клиенту)
                    //"quantity_now double," +
                    "UNIQUE (nomenclature_id, client_id)" +
                    ");"
        )

        db.execSQL(
            "create table salesL2 (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +  // здесь номенклатура или группа (в зависимости от настроек)
                    "nomenclature_id text," +
                    "client_id text," +
                    "distr_point_id text," +
                    "datedoc text," +
                    "numdoc text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE places (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id TEXT," +
                    "code TEXT," +
                    "descr TEXT," +
                    "isUsed int default 0," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE occupied_places (" +
                    "place_id TEXT," +
                    "client_id TEXT," +
                    "document_id TEXT," +
                    "datedoc TEXT," +
                    "document TEXT," +
                    "shipping_time TEXT," +
                    "shipping_date TEXT," +
                    "isUsed int default 0," +
                    "UNIQUE ('place_id', 'client_id','document_id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE journal (" +
                    "_id integer primary key autoincrement, " +  // 0-order
                    // 1-refund
                    // 2-payment (но их в журнале сейчас нет, они отдельно)
                    // 3-distribs
                    "iddocdef int," +
                    "order_id INT," +
                    "payment_id INT," +
                    "refund_id INT," +
                    "distribs_id INT," +
                    "uid text," +
                    "id text," +
                    "numdoc text," +
                    "datedoc text," +
                    "shipping_date text," +
                    "client_id text," +
                    "use_client_descr int," +
                    "client_descr text," +
                    "state int," +
                    "sum_doc double," +
                    "sum_shipping double default -1," +
                    "color text," +
                    "isUsed int default 0," +
                    "UNIQUE ('uid','id')," +
                    "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
                    ");"
        )


        //db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
        db.execSQL(
            "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                    "DELETE FROM journal WHERE order_id=OLD._id; " +
                    "END;"
        )


        //db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
        //      "FOR EACH ROW BEGIN " +
        //      "DELETE FROM journal WHERE payment_id=OLD._id; " +
        //      "END;");
        db.execSQL(
            "CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
                    "DELETE FROM journal WHERE refund_id=OLD._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
                    "DELETE FROM journal WHERE distribs_id=OLD._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                    "WHEN new.editing_backup=0 BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color=" + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " where order_id=new._id; " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color=" + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " where refund_id=new._id; " +
                    "END;"
        )


        db.execSQL(
            "CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
                    " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + "); " +
                    "END;"
        )

        db.execSQL(
            "CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
                    "FOR EACH ROW BEGIN " +
                    "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color=" + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " where distribs_id=new._id; " +
                    "END;"
        )

        db.execSQL(
            "create table gps_coord (" +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpstype int," +
                    "gpsaccuracy float," +
                    "version int" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE equipment (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id TEXT," +
                    "code TEXT," +
                    "descr TEXT," +
                    "flags INT," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "CREATE TABLE equipment_rests (" +
                    "_id integer primary key autoincrement," +
                    "client_id TEXT," +
                    "agreement_id TEXT," +
                    "nomenclature_id TEXT," +
                    "distr_point_id TEXT," +
                    "quantity DOUBLE," +
                    "sum DOUBLE," +
                    "flags INT," +
                    "doc_id TEXT," +
                    "doc_descr TEXT," +
                    "date TEXT," +
                    "datepast TEXT," +
                    "UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')" +
                    ");"
        )

        db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);")

        db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));")

        db.execSQL(
            "create table routes (" +
                    "_id integer primary key autoincrement, " +
                    "id text," +
                    "code text," +
                    "descr text," +
                    "manager_id text," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL(
            "create table routes_lines (" +
                    "_id integer primary key autoincrement, " +
                    "route_id text," +
                    "lineno int," +
                    "distr_point_id text," +
                    "visit_time text," +
                    "UNIQUE ('route_id', 'lineno')" +
                    ");"
        )

        db.execSQL(
            "create table routes_dates (" +
                    "_id integer primary key autoincrement, " +
                    "route_date text," +
                    "route_id text," +
                    "UNIQUE ('route_date')" +
                    ");"
        )

        //
        db.execSQL(
            "create table real_routes_dates (" +
                    "_id integer primary key autoincrement, " +
                    "uid text," +  // в КПК
                    "id text," +  // в 1С
                    "route_date text," +
                    "route_id text," +
                    "route_descr text," +
                    "UNIQUE ('route_date')" +
                    ");"
        )

        db.execSQL(
            "create table real_routes_lines (" +
                    "_id integer primary key autoincrement, " +
                    "real_route_id integer," +
                    "lineno int," +
                    "distr_point_id text," +
                    "required_visit_time text," +
                    "start_visit_time text," +
                    "end_visit_time text," +
                    "version int," +
                    "version_ack int," +
                    "versionPDA int," +
                    "versionPDA_ack int," +
                    "datecoord text," +
                    "latitude double," +
                    "longitude double," +
                    "gpsstate int," +
                    "gpsaccuracy float," +
                    "accept_coord int," +
                    "UNIQUE ('real_route_id', 'lineno')" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // есть еще onDowngrade

        Log.d(LOG_TAG, " --- onUpgrade database --- ")
        db.execSQL("delete from seances")

        if (oldVersion < 2) {
            db.execSQL(
                "create table settings (" +
                        "_id integer primary key autoincrement, " +
                        "fmt text" +
                        ");"
            )
            db.execSQL(
                "create table salesloaded (" +
                        "_id integer primary key autoincrement, " +
                        "ver int," +
                        "datedoc text," +
                        "numdoc text," +
                        "refdoc text," +
                        "curator_id text," +
                        "distr_point_id text," +
                        "nomenclature_id text," +
                        "quantity double," +  // здесь стоимость, а не цена
                        "price double" +
                        ");"
            )
            db.execSQL("alter table clients add column flags int")
        }
        if (oldVersion < 3) {
            db.execSQL(
                "create table sales_versions (" +
                        "param text," +
                        "ver int," +
                        "UNIQUE ('param')" +
                        ");"
            )
        }
        if (oldVersion < 4) {
            db.execSQL("alter table salesloaded add column client_id text")
        }

        if (oldVersion < 8) {
            db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature")
            db.execSQL("DROP TABLE nomenclature")
            db.execSQL(
                "CREATE TABLE Nomenclature (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id TEXT," +
                        "isFolder INT," +
                        "parent_id text," +
                        "code TEXT," +
                        "descr TEXT," +
                        "descrFull TEXT," +
                        "quant_1 TEXT," +
                        "quant_2 TEXT," +
                        "edizm_1_id TEXT," +
                        "edizm_2_id TEXT," +
                        "quant_k_1 DOUBLE," +
                        "quant_k_2 DOUBLE," +
                        "opt_price DOUBLE," +
                        "m_opt_price DOUBLE," +
                        "rozn_price DOUBLE," +
                        "incom_price DOUBLE," +
                        "IsInPrice INT," +
                        "flagWithoutDiscont INT," +
                        "weight_k_1 DOUBLE," +
                        "weight_k_2 DOUBLE," +
                        "min_quantity DOUBLE," +  // минимальное продаваемое количество за 1 раз
                        "multiplicity DOUBLE," +  // кратность количества (нельзя дробить)
                        "required_sales DOUBLE," +  // план продажи за месяц по каждому клиенту
                        "flags INT," +
                        "UNIQUE ('id')" +
                        ");"
            )
            db.execSQL("insert into nomenclature select * from temp_Nomenclature")
            db.execSQL("drop table temp_Nomenclature")
            db.execSQL(
                "update nomenclature set parent_id=?",
                arrayOf<String>(Constants.emptyID)
            )
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )
        }

        if (oldVersion < 9) {
            db.execSQL("alter table messages add column type_idx int")
            db.execSQL("alter table messages add column date1 text")
            db.execSQL("alter table messages add column date2 text")
            db.execSQL("alter table messages add column client_id text")
            db.execSQL("alter table messages add column nomenclature_id text")
        }

        if (oldVersion < 10) {
            db.execSQL("alter table nomenclature add column descr_lower text")
            db.execSQL("alter table clients add column descr_lower text")
        }

        if (oldVersion < 11) {
            db.execSQL("alter table messages add column agreement_id text")
            db.execSQL("alter table messages add column report text")
            db.execSQL("alter table nomenclature add column parent_id_0 text")
            db.execSQL("alter table nomenclature add column parent_id_1 text")
            db.execSQL("alter table nomenclature add column parent_id_2 text")
            db.execSQL("alter table nomenclature add column parent_id_3 text")
            db.execSQL("alter table nomenclature add column parent_id_4 text")

            db.execSQL("alter table clients add column parent_id_0 text")
            db.execSQL("alter table clients add column parent_id_1 text")
            db.execSQL("alter table clients add column parent_id_2 text")
            db.execSQL("alter table clients add column parent_id_3 text")
            db.execSQL("alter table clients add column parent_id_4 text")
        }

        if (oldVersion < 12) {
            // nomenclature_hierarchy создается всегда, см. ниже
        }

        if (oldVersion < 13) {
            db.execSQL("alter table nomenclature add column image_file text")
            db.execSQL("alter table nomenclature add column image_file_checksum INT")
        }

        if (oldVersion < 14) {
            db.execSQL("alter table orders add column shipping_type int")
            db.execSQL("alter table orders add column shipping_time text")
            db.execSQL("alter table orders add column shipping_begin_time text")
            db.execSQL("alter table orders add column shipping_end_time text")
        }

        if (oldVersion < 15) {
            db.execSQL("alter table messages add column isMark int")

            db.execSQL(
                "create table curators_price (" +
                        "_id integer primary key autoincrement, " +
                        "curator_id text," +
                        "nomenclature_id text," +
                        "priceProcent double," +
                        "priceAdd double," +
                        "UNIQUE ('curator_id', 'nomenclature_id')" +
                        ");"
            )
        }

        if (oldVersion < 17) {
            db.execSQL("CREATE TEMPORARY TABLE messages_backup as select * from messages")
            db.execSQL("DROP TABLE messages")

            db.execSQL(
                "create table messages (" +
                        "_id integer primary key autoincrement," +
                        "uid text," +
                        "sender_id text," +
                        "receiver_id text," +
                        "text text," +
                        "fname text," +
                        "datetime text," +
                        "acknowledged int," +
                        "ver int," +
                        "type_idx int," +
                        "date1 text," +
                        "date2 text," +
                        "client_id text," +
                        "agreement_id text," +
                        "nomenclature_id text," +
                        "report text," +
                        "isMark int default 0," +
                        "isMarkCnt int default 0," +
                        "UNIQUE('uid')" +
                        ");"
            )

            db.execSQL("insert into messages select _id,uid,sender_id,receiver_id,text,fname,datetime,acknowledged,ver,type_idx,date1,date2,client_id,agreement_id,nomenclature_id,report,0 isMark,0 isMarkCnt from messages_backup")

            db.execSQL("drop table messages_backup")

            db.execSQL("update messages set isMark=0, isMarkCnt=0")
        }

        if (oldVersion < 18) {
            db.execSQL("alter table ordersLines add column discount DOUBLE")
        }

        if (oldVersion < 19) {
            // надо запросить торговые точки, в них была ошибка
            db.execSQL("update versions set ver=-1 where ver>0 and param=\"D_POINTS\"")
        }

        if (oldVersion < 20) {
            db.execSQL("alter table orders add column comment_payment text")
        }

        if (oldVersion < 21) {
            db.execSQL(
                "create table simple_discounts (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "code text," +
                        "descr text," +
                        "priceProcent double," +
                        "UNIQUE ('id')" +
                        ");"
            )
        }

        if (oldVersion < 22) {
            db.execSQL("alter table orders add column simple_discount_id text")
            db.execSQL("alter table orders add column create_client int")
            db.execSQL("alter table orders add column create_client_descr text")
            db.execSQL("alter table orders add column create_client_surname text")
            db.execSQL("alter table orders add column create_client_firstname text")
            db.execSQL("alter table orders add column create_client_lastname text")
            db.execSQL("alter table orders add column place_num int")
            db.execSQL("alter table orders add column card_num int")
            db.execSQL("alter table orders add column pay_credit double")
            db.execSQL("alter table orders add column ticket_m double")
            db.execSQL("alter table orders add column ticket_w double")
            db.execSQL("alter table orders add column quant_m int")
            db.execSQL("alter table orders add column quant_w int")

            db.execSQL("alter table settings add column ticket_m double")
            db.execSQL("alter table settings add column ticket_w double")
            db.execSQL("alter table settings add column agent_id text")

            db.execSQL(
                "create table cash_payments (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "client_id text," +
                        "agreement_id text," +
                        "comment text," +
                        "comment_closing text," +
                        "state int," +
                        "curator_id text," +
                        "sum_doc double," +
                        "UNIQUE ('uid','id')" +
                        ");"
            )


            /*
                db.execSQL("create table debt_ext (" +
                          "_id integer primary key autoincrement, " +
                          "client_id text," +
                          "agreement_id text," +
                          "curator_id text," +
                          "document text," +
                          "plan_pay_date text," +
                          "isExpanded int,"+ // 0 - если общий долг, 1,2,3 и т.д - детальные записи
                          "sum_debt double," +
                          "UNIQUE ('client_id','agreement_id','curator_id','isExpanded')" +
                          ");");
                */
            db.execSQL("alter table clients add column isUsed int default 0")
            db.execSQL("alter table pricetypes add column isUsed int default 0")
            db.execSQL("alter table nomenclature add column isUsed int default 0")
            db.execSQL("alter table prices add column isUsed int default 0")
            db.execSQL("alter table simple_discounts add column isUsed int default 0")
            db.execSQL("alter table saldo add column isUsed int default 0")
            db.execSQL("alter table rests add column isUsed int default 0")
        }

        if (oldVersion < 23) {
            db.execSQL(
                "CREATE TABLE saldo_extended (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "client_id TEXT," +
                        "agreement_id TEXT," +
                        "document_id TEXT," +
                        "manager_id TEXT," +
                        "document_descr TEXT," +
                        "manager_descr TEXT," +
                        "saldo DOUBLE," +
                        "saldo_past DOUBLE," +
                        "isUsed int default 0," +
                        "UNIQUE ('client_id','agreement_id','document_id','manager_id')" +
                        ");"
            )
        }

        if (oldVersion < 24) {
            db.execSQL("alter table orders add column create_client_phone text")
            db.execSQL("alter table clients add column card_num text")
            db.execSQL("alter table clients add column phone_num text")

            db.execSQL("alter table saldo_extended add column agreement_descr text")
            db.execSQL("alter table saldo_extended add column organization_descr text")
            db.execSQL("alter table saldo_extended add column document_datetime text")

            db.execSQL("alter table orders add column quant_mw int")

            db.execSQL("alter table agreements add column default_manager_id text")
            db.execSQL("update agreements set default_manager_id=''")

            db.execSQL("alter table cash_payments add column manager_descr text")
            db.execSQL("alter table cash_payments add column organization_descr text")
        }

        if (oldVersion < 25) {
            db.execSQL(
                "create table ordersPlaces (" +
                        "_id integer primary key autoincrement, " +
                        "order_id integer," +  // внутренний код
                        "place_id text" +
                        ");"
            )

            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")

            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN DELETE FROM ordersLines WHERE order_id=OLD._id; " +
                        "DELETE FROM ordersPlaces WHERE order_id=OLD._id; END;"
            )

            db.execSQL(
                "CREATE TABLE places (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "id TEXT," +
                        "code TEXT," +
                        "descr TEXT," +
                        "isUsed int default 0," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "CREATE TABLE occupied_places (" +
                        "place_id TEXT," +
                        "client_id TEXT," +
                        "document_id TEXT," +
                        "datedoc TEXT," +
                        "document TEXT," +
                        "shipping_time TEXT," +
                        "shipping_date TEXT," +
                        "isUsed int default 0," +
                        "UNIQUE ('place_id', 'client_id','document_id')" +
                        ");"
            )

            db.execSQL("alter table nomenclature add column order_for_sorting INT")
            db.execSQL("update nomenclature set order_for_sorting=0")

            db.execSQL("alter table orders add column shipping_date text")
            db.execSQL("update orders set shipping_date=\"\"")

            db.execSQL("alter table ordersLines add column shipping_time text")
            db.execSQL("update ordersLines set shipping_time=\"\"")
        }

        if (oldVersion < 26) {
            db.execSQL("alter table orders add column manager_comment text")
            db.execSQL("update orders set manager_comment=\"\"")

            db.execSQL("alter table orders add column theme_comment text")
            db.execSQL("update orders set theme_comment=\"\"")

            db.execSQL("alter table orders add column phone_num text")
            db.execSQL("update orders set phone_num=\"\"")
        }

        if (oldVersion < 27) {
            db.execSQL("alter table ordersLines add column comment_in_line text")
            db.execSQL("update ordersLines set comment_in_line=\"\"")
        }

        if (oldVersion < 28) {
            db.execSQL("alter table nomenclature add column group_of_analogs INT")
            db.execSQL("update nomenclature set group_of_analogs=0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )


            db.execSQL(
                "create table refundsLines (" +
                        "_id integer primary key autoincrement, " +
                        "refund_id integer," +  // внутренний код
                        "nomenclature_id text," +
                        "client_id text," +
                        "quantity_requested double," +
                        "quantity double," +  //"price double," +
                        //"total double," +
                        //"discount DOUBLE,"+
                        "k double," +
                        "ed text," +
                        "comment_in_line text" +
                        ");"
            )

            db.execSQL(
                "create table refunds (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "base_doc_id text," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "client_id text," +
                        "agreement_id text," +
                        "distr_point_id text," +
                        "comment text," +
                        "comment_closing text," +
                        "closed_not_full int," +
                        "state int," +
                        "curator_id text," +
                        "bw int," +
                        "trade_type text," +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "accept_coord int," +
                        "dont_need_send int," +
                        "price_type_id text," +
                        "stock_id text," +
                        "sum_doc double," +
                        "weight_doc double," +
                        "simple_discount_id text," +
                        "UNIQUE ('uid','id')" +
                        ");"
            )

            db.execSQL("alter table agreements add column flags int")
            db.execSQL("update agreements set flags=0")
        }

        if (oldVersion < 31) {
            db.execSQL("alter table saldo add column saldo_past30 DOUBLE default 0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "SALDO")
            )
        }
        if (oldVersion < 32) {
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "SALDO")
            )
        }
        if (oldVersion < 34) {
            db.execSQL("alter table orders add column sum_shipping double default -1")
        }

        if (oldVersion < 35) {
            db.execSQL("alter table nomenclature add column nomenclature_color INT default 0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "NOMENCLATURE")
            )
        }

        if (oldVersion < 36) {
            db.execSQL("alter table cash_payments add column vicarious_power_id text")
            db.execSQL("alter table cash_payments add column vicarious_power_descr text")

            db.execSQL("update cash_payments set vicarious_power_id=''")
            db.execSQL("update cash_payments set vicarious_power_descr=''")

            db.execSQL(
                "create table vicarious_power (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "descr text," +
                        "numdoc text," +
                        "datedoc text," +
                        "date_action text," +
                        "comment text," +
                        "client_id text," +
                        "client_descr text," +
                        "agreement_id text," +
                        "agreement_descr text," +
                        "fio_descr text," +
                        "manager_id text," +
                        "manager_descr text," +
                        "organization_id text," +
                        "organization_descr text," +
                        "state text," +
                        "sum_doc double," +
                        "UNIQUE ('id')" +
                        ");"
            )
        }

        if (oldVersion < 37) {
            db.execSQL(
                "create table gps_coord (" +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "version int" +
                        ");"
            )

            db.execSQL("alter table settings add column gps_interval int default 0")
        }

        if (oldVersion < 38) {
            db.execSQL(
                "CREATE TABLE distribsContracts (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id TEXT," +
                        "position int," +
                        "code TEXT," +
                        "descr TEXT," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "create table distribsLines (" +
                        "_id integer primary key autoincrement, " +
                        "distribs_id integer," +  // внутренний код
                        "contract_id text," +
                        "quantity double" +
                        ");"
            )

            db.execSQL(
                "create table distribs (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "client_id text," +
                        "curator_id text," +
                        "distr_point_id text," +
                        "state int," +
                        "comment text," +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "accept_coord int," +
                        "UNIQUE ('uid','id')" +
                        ");"
            )
        }

        if (oldVersion < 39) {
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_update")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS refunds_on_update")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS distribs_on_update")


            // из-за того, что непонятно, как можно добавить distribs_id в UNIQUE,
            // удаляем журнал и снова его создаем. заполнится он при последующей реиндексации
            db.execSQL("DROP TABLE IF EXISTS journal")
            db.execSQL(
                "CREATE TABLE journal (" +
                        "_id integer primary key autoincrement, " +  // 0-order
                        // 1-refund
                        // 2-payment (но их в журнале сейчас нет, они отдельно)
                        // 3-distribs
                        "iddocdef int," +
                        "order_id INT," +
                        "payment_id INT," +
                        "refund_id INT," +
                        "distribs_id INT," +
                        "uid text," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "shipping_date text," +
                        "client_id text," +
                        "use_client_descr int," +
                        "client_descr text," +
                        "state int," +
                        "sum_doc double," +
                        "sum_shipping double default -1," +
                        "color text," +
                        "isUsed int default 0," +
                        "UNIQUE ('uid','id')," +
                        "UNIQUE (order_id, payment_id, refund_id, distribs_id)" +
                        ");"
            )


            //db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete");
            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                        "DELETE FROM journal WHERE order_id=OLD._id; " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER payments_on_delete BEFORE DELETE ON cash_payments " +
            //      "FOR EACH ROW BEGIN " +
            //      "DELETE FROM journal WHERE payment_id=OLD._id; " +
            //      "END;");
            db.execSQL(
                "CREATE TRIGGER refunds_on_delete BEFORE DELETE ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM refundsLines WHERE refund_id=OLD._id; " +
                        "DELETE FROM journal WHERE refund_id=OLD._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER distribs_on_delete BEFORE DELETE ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM distribsLines WHERE distribs_id=OLD._id; " +
                        "DELETE FROM journal WHERE distribs_id=OLD._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color=" + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " where order_id=new._id; " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER refunds_on_insert AFTER INSERT ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (1, 0, 0, new._id, 0, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, new.sum_doc, -1, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER refunds_on_update AFTER UPDATE ON refunds " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=1, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=new.sum_doc, sum_shipping=-1, color=" + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " where refund_id=new._id; " +
                        "END;"
            )


            db.execSQL(
                "CREATE TRIGGER distribs_on_insert AFTER INSERT ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values" +
                        " (3, 0, 0, 0, new._id, new.uid, new.id, new.numdoc, new.datedoc, '', new.client_id, \"{\"||new.client_id||\"}\", new.state, 0, -1, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )

            db.execSQL(
                "CREATE TRIGGER distribs_on_update AFTER UPDATE ON distribs " +
                        "FOR EACH ROW BEGIN " +
                        "update journal set uid=new.uid, id=new.id, iddocdef=3, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date='', client_id=new.client_id, client_descr=\"{\"||new.client_id||\"}\", state=new.state, sum_doc=0, sum_shipping=-1, color=" + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " where distribs_id=new._id; " +
                        "END;"
            )
        }

        if (oldVersion < 41) {
            db.execSQL(
                "CREATE TABLE equipment (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "id TEXT," +
                        "code TEXT," +
                        "descr TEXT," +
                        "flags INT," +
                        "UNIQUE ('id')" +
                        ");"
            )
            /*
                  db.execSQL("CREATE TABLE equipment_rests (" +
                            "_id integer primary key autoincrement,"+
                            "client_id TEXT,"+
                            "nomenclature_id TEXT,"+
                            "distr_point_id TEXT,"+
                            "quantity DOUBLE,"+
                            "sum DOUBLE,"+
                            "flags INT,"+
                            "UNIQUE ('client_id', 'nomenclature_id', 'distr_point_id')"+
                            ");");
                    */
        }

        if (oldVersion < 42) {
            /*
                db.execSQL("drop table if exists equipment_rests");
                db.execSQL("CREATE TABLE equipment_rests (" +
                        "_id integer primary key autoincrement,"+
                        "client_id TEXT,"+
                        "agreement_id TEXT,"+
                        "nomenclature_id TEXT,"+
                        "distr_point_id TEXT,"+
                        "quantity DOUBLE,"+
                        "sum DOUBLE,"+
                        "flags INT,"+
                        "UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id')"+
                        ");");
                db.execSQL("update versions set ver=? where param=?", new String[]{"-1", "EQUIPMENT_RESTS"});
                */
        }

        if (oldVersion < 43) {
            db.execSQL("alter table orders add column datecreation text default \"\"")
            db.execSQL("alter table orders add column gpsstate int default 0")
            db.execSQL("alter table orders add column gpsaccuracy float default 0")
            db.execSQL("alter table refunds add column datecreation text default \"\"")
            db.execSQL("alter table refunds add column gpsstate int default 0")
            db.execSQL("alter table refunds add column gpsaccuracy float default 0")
            db.execSQL("alter table distribs add column gpsstate int default 0")
            db.execSQL("alter table distribs add column gpsaccuracy float default 0")
            db.execSQL("alter table gps_coord add column gpsstate int default 0")
            db.execSQL("alter table gps_coord add column gpsaccuracy float default 0")
        }

        if (oldVersion < 44) {
            db.execSQL("drop TABLE IF EXISTS equipment_rests")

            db.execSQL(
                "CREATE TABLE equipment_rests (" +
                        "_id integer primary key autoincrement," +
                        "client_id TEXT," +
                        "agreement_id TEXT," +
                        "nomenclature_id TEXT," +
                        "distr_point_id TEXT," +
                        "quantity DOUBLE," +
                        "sum DOUBLE," +
                        "flags INT," +
                        "doc_id TEXT," +
                        "doc_descr TEXT," +
                        "date TEXT," +
                        "datepast TEXT," +
                        "UNIQUE ('client_id', 'agreement_id', 'nomenclature_id', 'distr_point_id', 'doc_id')" +
                        ");"
            )

            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "EQUIPMENT_RESTS")
            )
        }

        if (oldVersion < 46) {
            db.execSQL("alter table ordersLines add column lineno int")
            db.execSQL("alter table orders add column editing_backup int")
            db.execSQL("alter table orders add column old_id int")
            db.execSQL("alter table distr_points add column price_type_id text")

            db.execSQL(
                "update orders set editing_backup=0, old_id=0, price_type_id=?",
                arrayOf<String>(
                    Constants.emptyID
                )
            )
            db.execSQL("update ordersLines set lineno=0")
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "D_POINTS")
            )
        }
        if (oldVersion < 47) {
            // Меняется только один триггер (insert), но т.к. таблицу переименовываем, и удаляем,
            // непонятно, что может произойти с триггером в разных версиях андроида, поэтому пересоздадим все
            //
            // ну и есть подозрение, что очищаются записи в журнале при удалении таблицы
            // поэтому код удаления перенес выше в версии 3.63
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_delete")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_insert")
            db.execSQL("DROP TRIGGER IF EXISTS orders_on_update")


            //SELECT name FROM sqlite_master WHERE type == 'index' AND lower(tbl_name) == 'orders'
            db.execSQL("ALTER TABLE orders RENAME TO orders_old")
            db.execSQL(
                "create table orders (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "id text," +
                        "numdoc text," +
                        "datedoc text," +
                        "client_id text," +
                        "agreement_id text," +
                        "distr_point_id text," +
                        "comment text," +
                        "comment_closing text," +
                        "comment_payment text," +
                        "closed_not_full int," +
                        "state int," +
                        "curator_id text," +
                        "bw int," +
                        "trade_type text," +
                        "datecreation text," +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "gpsstate int," +
                        "gpsaccuracy float," +
                        "accept_coord int," +
                        "dont_need_send int," +
                        "price_type_id text," +
                        "stock_id text," +
                        "sum_doc double," +
                        "weight_doc double," +
                        "shipping_type int," +
                        "shipping_time int," +
                        "shipping_begin_time text," +
                        "shipping_end_time text," +
                        "shipping_date text," +
                        "simple_discount_id text," +
                        "create_client int," +
                        "create_client_descr text," +
                        "create_client_surname text," +
                        "create_client_firstname text," +
                        "create_client_lastname text," +
                        "create_client_phone text," +
                        "place_num int," +
                        "card_num int," +
                        "pay_credit double," +
                        "ticket_m double," +
                        "ticket_w double," +
                        "quant_m int," +
                        "quant_w int," +
                        "quant_mw int," +
                        "manager_comment text," +
                        "theme_comment text," +
                        "phone_num text," +
                        "sum_shipping double," +  // 0-документ в нормальном состоянии
                        // 1-новый документ, записать не успели
                        // 2-документ начали редактировать, но не записали и не отменили изменения
                        "editing_backup int," +  // для состояния 2 - _id редактируемого документа
                        // в других случаях - не важно
                        "old_id int" +  // До 19.02.2018 editing_backup тут не было
                        //"UNIQUE ('uid','id')" +
                        ");"
            )

            db.execSQL("PRAGMA foreign_keys=off")
            //db.execSQL("insert into orders select * from orders_old");
            // Важно перечислить поля, т.к. не у всех порядок полей совпадает (из-за обновлений старых версий)
            val all_fields_list =
                "_id,uid,version,version_ack,versionPDA,versionPDA_ack,id,numdoc,datedoc,client_id,agreement_id,distr_point_id," +
                        "comment,comment_closing,comment_payment,closed_not_full,state,curator_id,bw,trade_type," +
                        "datecreation,datecoord,latitude,longitude,gpsstate,gpsaccuracy,accept_coord,dont_need_send," +
                        "price_type_id,stock_id,sum_doc,weight_doc,shipping_type,shipping_time,shipping_begin_time,shipping_end_time," +
                        "shipping_date,simple_discount_id,create_client,create_client_descr,create_client_surname," +
                        "create_client_firstname,create_client_lastname,create_client_phone,place_num,card_num,pay_credit,ticket_m,ticket_w,quant_m," +
                        "quant_w,quant_mw,manager_comment,theme_comment,phone_num,sum_shipping,editing_backup,old_id"
            db.execSQL("insert into orders (" + all_fields_list + ") select " + all_fields_list + " from orders_old order by _id")
            db.execSQL("PRAGMA foreign_keys=on")
            // С 19.02.2018
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS OrdersUniqueIndex ON orders ('uid','id','editing_backup')")
            db.execSQL("drop table orders_old")

            db.execSQL(
                "CREATE TRIGGER orders_on_delete BEFORE DELETE ON orders " +
                        "FOR EACH ROW BEGIN " +
                        "DELETE FROM ordersLines WHERE order_id=OLD._id; " +  //      "DELETE FROM ordersPlaces WHERE order_id=OLD._id; " +
                        "DELETE FROM journal WHERE order_id=OLD._id; " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
            //        "WHEN new.editing_backup=0 BEGIN " +
            //        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, "+JOURNAL_ORDER_COLOR_FOR_TRIGGER+"); " +
            //        "END;");
            db.execSQL(
                "CREATE TRIGGER orders_on_insert AFTER INSERT ON orders " +
                        "WHEN new.editing_backup=0 BEGIN " +
                        "INSERT INTO journal (iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color) values (0, new._id, 0, 0, 0, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + "); " +
                        "END;"
            )


            //db.execSQL("CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
            //        "FOR EACH ROW BEGIN " +
            //        "update journal set uid=new.uid, id=new.id, iddocdef=0, numdoc=new.numdoc, datedoc=new.datedoc, shipping_date=new.shipping_date, client_id=new.client_id, client_descr=case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, state=new.state, sum_doc=new.sum_doc, sum_shipping=new.sum_shipping, color="+JOURNAL_ORDER_COLOR_FOR_TRIGGER+" where order_id=new._id; " +
            //        "END;");
            db.execSQL(
                "CREATE TRIGGER orders_on_update AFTER UPDATE ON orders " +
                        "WHEN new.editing_backup=0 BEGIN " +
                        "insert or replace into journal (uid, id, iddocdef, numdoc, datedoc, shipping_date, client_id, client_descr, state, sum_doc, sum_shipping, color, order_id, payment_id, refund_id, distribs_id) values (new.uid, new.id, 0, new.numdoc, new.datedoc, new.shipping_date, new.client_id, case when new.create_client=1 then new.create_client_descr else \"{\"||new.client_id||\"}\" end, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + ", new._id, 0, 0, 0); " +
                        "END;"
            )
        }
        if (oldVersion < 48) {
            db.execSQL("CREATE TABLE mtradelog (_id integer primary key autoincrement, messagetext text, messagetype int, version int);")
        }

        if (oldVersion < 49) {
            db.execSQL("CREATE TABLE permissions_requests (_id integer primary key autoincrement, permission_name text, datetime text, seance_incoming text, UNIQUE('permission_name'));")
        }

        if (oldVersion < 50) {
            // Поле добавится, но в разделе UNIQUE это поле прописано не будет
            // но т.к. у этой организации, где это важно, будет установлена новая версия,
            // а у всех остальных это поле всегда будет пустым, то это вообще не важно
            db.execSQL("alter table saldo_extended add column agreement30_id text")
            db.execSQL("update saldo_extended set agreement30_id=''")

            db.execSQL(
                "create table agreements30 (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "owner_id text," +
                        "organization_id text," +
                        "code text," +
                        "descr text," +
                        "price_type_id text," +
                        "default_manager_id text," +
                        "sale_id text," +
                        "kredit_days int," +
                        "kredit_sum double," +
                        "flags int," +
                        "UNIQUE ('id', 'owner_id')" +
                        ");"
            )

            db.execSQL("alter table orders add column agreement30_id text")
            db.execSQL("update orders set agreement30_id=''")

            db.execSQL(
                "create table prices_agreements30 (" +
                        "_id integer primary key autoincrement, " +
                        "agreement30_id text," +
                        "nomenclature_id text," +
                        "pack_id text," +  // Пока не используется
                        "ed_izm_id text," +
                        "edIzm text," +
                        "price double," +
                        "k double," +
                        "UNIQUE ('agreement30_id', 'nomenclature_id')" +
                        ");"
            )
        }

        if (oldVersion < 51) {
            db.execSQL("alter table orders add column gpstype int")
            db.execSQL("alter table distribs add column gpstype int")
            db.execSQL("alter table gps_coord add column gpstype int")

            db.execSQL("update orders set gpstype=gpsstate")
            db.execSQL("update distribs set gpstype=gpsstate")
            db.execSQL("update gps_coord set gpstype=gpsstate")

            db.execSQL("alter table cash_payments add column datecreation text")
            db.execSQL("alter table cash_payments add column latitude text")
            db.execSQL("alter table cash_payments add column longitude text")
            db.execSQL("alter table cash_payments add column gpsstate int")
            db.execSQL("alter table cash_payments add column gpsaccuracy float")
            db.execSQL("alter table cash_payments add column gpstype int")

            db.execSQL("update cash_payments set datecreation=''")
            db.execSQL("update cash_payments set latitude=0.0")
            db.execSQL("update cash_payments set longitude=0.0")
            db.execSQL("update cash_payments set gpsstate=0")
            db.execSQL("update cash_payments set gpsaccuracy=0.0")
            db.execSQL("update cash_payments set gpstype=0")
        }

        if (oldVersion < 52) {
            db.execSQL("alter table cash_payments add column datecoord text")
            db.execSQL("update cash_payments set datecoord=''")
        }

        if (oldVersion < 53) {
            db.execSQL("alter table cash_payments add column accept_coord int")
            db.execSQL("update cash_payments set accept_coord=0")
        }

        if (oldVersion < 54) {
            db.execSQL("alter table nomenclature add column image_width int")
            db.execSQL("alter table nomenclature add column image_height int")
        }
        if (oldVersion < 55) {
            db.execSQL("alter table nomenclature add column image_file_size int")
        }

        if (oldVersion < 63) {
            db.execSQL(
                "create table routes (" +
                        "_id integer primary key autoincrement, " +
                        "id text," +
                        "code text," +
                        "descr text," +
                        "manager_id text," +
                        "UNIQUE ('id')" +
                        ");"
            )

            db.execSQL(
                "create table routes_dates (" +
                        "_id integer primary key autoincrement, " +
                        "route_date text," +
                        "route_id text," +
                        "UNIQUE ('route_date')" +
                        ");"
            )

            db.execSQL(
                "create table routes_lines (" +
                        "_id integer primary key autoincrement, " +
                        "route_id text," +
                        "lineno int," +
                        "distr_point_id text," +
                        "visit_time text," +
                        "UNIQUE ('route_id', 'lineno')" +
                        ");"
            )
            //
            db.execSQL(
                "create table real_routes_dates (" +
                        "_id integer primary key autoincrement, " +
                        "uid text," +  // в КПК
                        "id text," +  // в 1С
                        "route_date text," +
                        "route_id text," +
                        "route_descr text," +
                        "UNIQUE ('route_date')" +
                        ");"
            )

            db.execSQL(
                "create table real_routes_lines (" +
                        "_id integer primary key autoincrement, " +
                        "real_route_id integer," +
                        "lineno int," +
                        "distr_point_id text," +
                        "required_visit_time text," +
                        "start_visit_time text," +
                        "end_visit_time text," +
                        "version int," +
                        "version_ack int," +
                        "versionPDA int," +
                        "versionPDA_ack int," +
                        "datecoord text," +
                        "latitude double," +
                        "longitude double," +
                        "UNIQUE ('real_route_id', 'lineno')" +
                        ");"
            )

            db.execSQL("alter table cash_payments add column distr_point_id text")
        }

        if (oldVersion < 64) {
            db.execSQL("alter table real_routes_lines add column gpsstate int")
            db.execSQL("alter table real_routes_lines add column gpsaccuracy float")
            db.execSQL("alter table real_routes_lines add column accept_coord int")
            db.execSQL("update real_routes_lines set gpsstate=0")
            db.execSQL("update real_routes_lines set gpsaccuracy=0.0")
            db.execSQL("update real_routes_lines set accept_coord=0")
        }

        if (oldVersion < 66) {
            db.execSQL("alter table refunds add column shipping_type int")
            db.execSQL("update refunds set shipping_type=1")
        }

        if (oldVersion < 69) {
            //db.execSQL("alter table Rests add column organization_id text default ''");
            //db.execSQL("update Rests set organization_id=''");

            db.execSQL("CREATE TEMPORARY TABLE temp_Rests as select * from Rests")
            db.execSQL("DROP TABLE Rests")
            db.execSQL("alter table temp_Rests add column organization_id int")
            // Можно было EmptyID записать туда
            db.execSQL("update temp_Rests set organization_id=''")

            db.execSQL(
                "CREATE TABLE Rests (" +
                        "stock_id TEXT," +
                        "nomenclature_id TEXT," +
                        "organization_id TEXT default ''," +
                        "quantity DOUBLE," +
                        "quantity_reserve DOUBLE," +
                        "isUsed int default 0," +
                        "UNIQUE ('stock_id', 'nomenclature_id','organization_id')" +
                        ");"
            )

            db.execSQL("insert into Rests select * from temp_Rests")
            db.execSQL("drop table temp_Rests")
            // В принципе это не обязательно для всех, кроме Тандема (а они все равно начинают сначала)
            db.execSQL(
                "update versions set ver=? where param=?",
                arrayOf<String>("-1", "RESTS")
            )
        }

        if (oldVersion < 70) {
            db.execSQL("alter table clients add column email_for_cheques text")
            db.execSQL("update clients set email_for_cheques=''")
        }

        if (oldVersion < 71) {
            db.execSQL("alter table settings add column agent_price_type_id text")
        }

        if (oldVersion < 72) {
            db.execSQL("alter table nomenclature add column compose_with text")
        }

        if (oldVersion < 73) {
            // есть подозрение, что там может быть null
            // при загрузке из текстовых файлов, в случае, когда это группа
            db.execSQL("update nomenclature set flags=0")

            //                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//                    // В старом формате это была строка, в новом - Long
//                    String oldDate=sharedPreferences.getString("work_date", "");
//                    if (!oldDate.isEmpty()) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.remove("work_date");
//                        try {
//                            Date newDate=new SimpleDateFormat("yyyy.MM.dd").parse(oldDate);
//                            editor.putLong("work_date_l", newDate.getTime());
//                        } catch (ParseException e) {
//                        }
//                        editor.commit();
//                    }
//
//                    String oldDate2=sharedPreferences.getString("start_date_for_occupied_places", "");
//                    if (!oldDate2.isEmpty()) {
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.remove("start_date_for_occupied_places");
//                        try {
//                            Date newDate=new SimpleDateFormat("yyyy.MM.dd").parse(oldDate2);
//                            editor.putLong("start_date_for_occupied_places_l", newDate.getTime());
//                        } catch (ParseException e) {
//                        }
//                        editor.commit();
//                    }
        }

        if (oldVersion < 74) {
            db.execSQL("alter table nomenclature add column backorder int default 0")
            db.execSQL("update nomenclature set backorder=0")
        }

        if (oldVersion < 75) {
            db.execSQL("alter table agreements30 add column default_stock_id text default ''")
            db.execSQL("update agreements30 set default_stock_id=''")
        }


        /*
            if (oldVersion<43)
            {
                  db.execSQL("drop TABLE equipment_rests");

                  db.execSQL("CREATE TABLE equipment_rests (" +
                            "_id integer primary key autoincrement,"+
                            "client_id TEXT,"+
                            "nomenclature_id TEXT,"+
                            "distr_point_id TEXT,"+
                            "quantity DOUBLE,"+
                            "sum DOUBLE,"+
                            "flags INT,"+
                            "UNIQUE ('client_id', 'nomenclature_id', 'distr_point_id')"+
                            ");");
            }
            */

        // TODO
        // удалить колонку create_client_phone

        // Продажи в телефоне (текущие)
        db.execSQL("drop view if exists salesVB")

        /*
            db.execSQL("CREATE VIEW salesVB as select " +
                    "nomenclature_id, "+
                    "client_id, " +
                    "sum(quantity) as quantity_saled from orders " +
                    "join ordersLines on ordersLines.order_id=orders._id " +
                    "where orders.state="+E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value()+" "+
                    "group by nomenclature_id, client_id;");
            */

        // Продажи в 1С (история)
        db.execSQL("drop view if exists salesV")

        // То же самое, для окна ввода количества
        //db.execSQL("drop view if exists salesGroupsV");
        /*
            db.execSQL("CREATE VIEW salesV as select " +
                    "nomenclature_id,"+
                    "client_id,"+
                    "sum(quantity) as quantity_saled "+
                    "from salesloaded "+
                    "group by nomenclature_id, client_id;");
            */

        // Продажи в 1С и в КПК вместе, понедельно, с 30.11.2020
        db.execSQL("drop view if exists salesV_7")

        // Эти таблицы пересоздаем всегда
        db.execSQL("drop table if exists nomenclature_hierarchy")
        db.execSQL(
            "create table nomenclature_hierarchy (" +
                    "id text," +
                    "ord_idx int," +
                    "groupDescr text," +
                    "level int," +
                    "level0_id text," +
                    "level1_id text," +
                    "level2_id text," +
                    "level3_id text," +
                    "level4_id text," +
                    "level5_id text," +
                    "level6_id text," +
                    "level7_id text," +
                    "level8_id text," +
                    "dont_use_in_hierarchy int," +
                    "UNIQUE ('id')" +
                    ");"
        )

        db.execSQL("drop table if exists salesL")
        db.execSQL(
            "create table salesL (" +
                    "nomenclature_id text," +
                    "client_id text," +  //"distr_point_id text," +
                    "strQuantity text," +
                    "quantity double," +  //"quantity_now double," +
                    "UNIQUE (nomenclature_id, client_id)" +
                    ");"
        )

        db.execSQL("drop table if exists salesL2")
        db.execSQL(
            "create table salesL2 (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +  // здесь номенклатура или группа (в зависимости от настроек)
                    "nomenclature_id text," +
                    "client_id text," +  // 20.09.2019
                    "distr_point_id text," +  //
                    "datedoc text," +
                    "numdoc text," +
                    "quantity double," +  // здесь стоимость, а не цена
                    "price double" +
                    ");"
        )

        // ^^^^ таблицы истории продаж перезаполняются при изменении версии базы из MainActivity
        db.execSQL(
            String.format(
                "insert into seances(incoming) values ('UPGRADED%d')",
                oldVersion
            )
        )
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //
        onUpgrade(db, oldVersion, newVersion)
    }

    fun Reindex() {
        val db = dbHelper!!.getWritableDatabase()

        db.execSQL("DROP INDEX IF EXISTS Clients_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Clients_parent_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Clients_isFolder_idx")
        db.execSQL("DROP INDEX IF EXISTS organizations_idx")
        db.execSQL("DROP INDEX IF EXISTS agreements_id_idx")
        db.execSQL("DROP INDEX IF EXISTS agreements_owner_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_parent_id_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_isFolder_idx")
        db.execSQL("DROP INDEX IF EXISTS Nomenclature_descr_idx")
        db.execSQL("DROP INDEX IF EXISTS Rests_nomenclature_stocks_idx")
        db.execSQL("DROP INDEX IF EXISTS prices_nomenclature_price_idx")
        db.execSQL("DROP INDEX IF EXISTS clients_price_client_idx")
        db.execSQL("DROP INDEX IF EXISTS curators_price_curator_idx")
        db.execSQL("DROP INDEX IF EXISTS agents_idx")
        db.execSQL("DROP INDEX IF EXISTS curators_idx")
        db.execSQL("DROP INDEX IF EXISTS Saldo_client_idx")
        db.execSQL("DROP INDEX IF EXISTS saldo_extended_client_idx")
        db.execSQL("DROP INDEX IF EXISTS distr_points_idx")
        db.execSQL("DROP INDEX IF EXISTS OrdersLines_idx")
        db.execSQL("DROP INDEX IF EXISTS OrdersLines_sales")
        db.execSQL("DROP INDEX IF EXISTS orders_idx")
        db.execSQL("DROP INDEX IF EXISTS orders_datedoc")
        db.execSQL("DROP INDEX IF EXISTS orders_uid")
        db.execSQL("DROP INDEX IF EXISTS orders_state")
        db.execSQL("DROP INDEX IF EXISTS salesloaded_idx")
        db.execSQL("DROP INDEX IF EXISTS unique_salesloaded_idx")
        db.execSQL("DROP INDEX IF EXISTS journal_order_id")
        db.execSQL("DROP INDEX IF EXISTS journal_payment_id")
        db.execSQL("DROP INDEX IF EXISTS journal_refund_id")
        db.execSQL("DROP INDEX IF EXISTS salesL2_idx")
        //db.execSQL("DROP INDEX IF EXISTS salesL2_no_dp_idx");
        db.execSQL("DROP INDEX IF EXISTS equipment_rests_client_id")
        db.execSQL("DROP INDEX IF EXISTS agreements30_owner_id")

        db.execSQL("CREATE INDEX Clients_id_idx ON Clients (id ASC);")
        db.execSQL("CREATE INDEX Clients_parent_id_idx ON Clients (parent_id ASC);")
        db.execSQL("CREATE INDEX Clients_isFolder_idx ON Clients (isFolder ASC);")
        db.execSQL("CREATE INDEX organizations_idx ON organizations (id ASC);")
        db.execSQL("CREATE INDEX agreements_id_idx ON agreements (id ASC);")
        db.execSQL("CREATE INDEX agreements_owner_id_idx ON agreements (owner_id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_id_idx ON Nomenclature (id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_parent_id_idx ON Nomenclature (parent_id ASC);")
        db.execSQL("CREATE INDEX Nomenclature_isFolder_idx ON Nomenclature (isFolder ASC);")
        db.execSQL("CREATE INDEX Nomenclature_descr_idx ON Nomenclature (descr ASC);")
        db.execSQL("CREATE INDEX Rests_nomenclature_stocks_idx ON Rests (nomenclature_id ASC, stock_id ASC);")
        db.execSQL("CREATE INDEX prices_nomenclature_price_idx ON prices (nomenclature_id ASC, price_type_id ASC);")
        db.execSQL("CREATE INDEX clients_price_client_idx ON clients_price (client_id ASC);")
        db.execSQL("CREATE INDEX curators_price_curator_idx ON curators_price (curator_id ASC);")
        db.execSQL("CREATE INDEX agents_idx ON agents (id ASC);")
        db.execSQL("CREATE INDEX curators_idx ON curators (id ASC);")
        db.execSQL("CREATE INDEX Saldo_client_idx ON Saldo (client_id ASC);")
        db.execSQL("CREATE INDEX saldo_extended_client_idx ON saldo_extended (client_id ASC);")
        db.execSQL("CREATE INDEX distr_points_idx ON distr_points (id ASC);")
        db.execSQL("CREATE INDEX OrdersLines_idx ON ordersLines (order_id ASC);")
        db.execSQL("CREATE INDEX OrdersLines_sales ON ordersLines (nomenclature_id,client_id ASC);")
        db.execSQL("CREATE INDEX orders_idx ON orders (id ASC);")
        db.execSQL("CREATE INDEX orders_datedoc ON orders (datedoc ASC);")
        db.execSQL("CREATE INDEX orders_uid ON orders (uid ASC);")
        db.execSQL("CREATE INDEX orders_state ON orders (state ASC, accept_coord ASC);")
        db.execSQL("CREATE INDEX salesloaded_idx ON salesloaded (nomenclature_id ASC, client_id ASC, distr_point_id ASC, datedoc ASC);")
        db.execSQL("CREATE INDEX unique_salesloaded_idx ON salesloaded (nomenclature_id ASC, refdoc ASC);")
        db.execSQL("CREATE INDEX journal_order_id ON journal (order_id ASC);")
        db.execSQL("CREATE INDEX journal_payment_id ON journal (payment_id ASC);")
        db.execSQL("CREATE INDEX journal_refund_id ON journal (refund_id ASC);")
        db.execSQL("CREATE INDEX equipment_rests_client_id ON equipment_rests (client_id ASC, agreement_id ASC);")
        //db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, distr_point_id ASC, nomenclature_id ASC, datedoc ASC);");
        //db.execSQL("CREATE INDEX salesL2_no_dp_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);");
        db.execSQL("CREATE INDEX salesL2_idx ON salesL2 (client_id ASC, nomenclature_id ASC, datedoc ASC);")
        db.execSQL("CREATE INDEX agreements30_owner_id ON agreements30 (owner_id ASC);")

        // TODO сделать удаление несуществующих и добавление новых
        // (из-за того, что таким образом нарушится сортировка, т.е. порядок ввода документов)
        //db.execSQL("delete from journal");
        db.execSQL("update journal set isUsed=0")

        //db.execSQL("INSERT OR REPLACE INTO journal select j._id, d._id as order_id, 0 as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, d.create_client as use_client_descr, d.create_client_descr as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from orders d left join journal j on j.order_id=d._id;");
        ////db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, d._id as payment_id, 0 as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from cash_payments d join journal j on j.payment_id=d._id;");
        //db.execSQL("INSERT OR REPLACE INTO journal select j._id, 0 as order_id, 0 as payment_id, d._id as refund_id, d.uid, d.id, d.numdoc, d.datedoc, d.client_id, 0 as use_client_descr, \"\" as client_descr, d.state, d.sum_doc, '0' as color, 1 as isUsed from refunds d left join journal j on j.refund_id=d._id;");
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 0 as iddocdef, new._id as order_id, 0 as payment_id, 0 as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, new.shipping_date as shipping_date, new.client_id, new.create_client as use_client_descr, new.create_client_descr as client_descr, new.state, new.sum_doc, new.sum_shipping, " + JOURNAL_ORDER_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from orders new left join journal j on j.order_id=new._id where editing_backup=0;")
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 1 as iddocdef, 0 as order_id, 0 as payment_id, new._id as refund_id, 0 as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, new.sum_doc, 0 as sum_shipping, " + JOURNAL_REFUND_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from refunds new left join journal j on j.refund_id=new._id;")
        db.execSQL("INSERT OR REPLACE INTO journal (_id, iddocdef, order_id, payment_id, refund_id, distribs_id, uid, id, numdoc, datedoc, shipping_date, client_id, use_client_descr, client_descr, state, sum_doc, sum_shipping, color, isUsed) select j._id, 3 as iddocdef, 0 as order_id, 0 as payment_id, 0 as refund_id, new._id as distribs_id, new.uid, new.id, new.numdoc, new.datedoc, '' as shipping_date,                new.client_id, 0 as use_client_descr, \"\" as client_descr, new.state, 0 as sum_doc, 0 as sum_shipping, " + JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER + " as color, 1 as isUsed from distribs new left join journal j on j.distribs_id=new._id;")

        db.execSQL("delete from journal where isUsed=0")
    }

    fun Vacuum() {
        val db = dbHelper!!.getWritableDatabase()
        db.execSQL("vacuum")
    }
}

private var dbHelper: DBHelper? = null

override fun delete(uri: Uri, where: String?, selectionArgs: Array<String?>?): Int {
    var where = where
    when (sUriMatcher.match(uri)) {
        URI_NOMENCLATURE -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(NOMENCLATURE_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(NOMENCLATURE_CONTENT_URI, null)
            return cnt
        }

        URI_NOMENCLATURE_HIERARCHY -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(NOMENCLATURE_HIERARCHY_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver()
                .notifyChange(NOMENCLATURE_HIERARCHY_CONTENT_URI, null)
            return cnt
        }

        URI_PLACES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(PLACES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(PLACES_CONTENT_URI, null)
            return cnt
        }

        URI_OCCUPIED_PLACES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(OCCUPIED_PLACES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(OCCUPIED_PLACES_CONTENT_URI, null)
            return cnt
        }

        URI_CLIENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(CLIENTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(CLIENTS_CONTENT_URI, null)
            return cnt
        }

        URI_AGREEMENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(AGREEMENTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(AGREEMENTS_CONTENT_URI, null)
            return cnt
        }

        URI_AGREEMENTS30 -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(AGREEMENTS30_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(AGREEMENTS30_CONTENT_URI, null)
            return cnt
        }

        URI_SALDO -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(SALDO_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(SALDO_CONTENT_URI, null)
            return cnt
        }

        URI_SALDO_EXTENDED -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(SALDO_EXTENDED_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(SALDO_EXTENDED_CONTENT_URI, null)
            return cnt
        }

        URI_RESTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(RESTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(RESTS_CONTENT_URI, null)
            return cnt
        }

        URI_ORGANIZATIONS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ORGANIZATIONS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ORGANIZATIONS_CONTENT_URI, null)
            return cnt
        }

        URI_STOCKS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(STOCKS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(STOCKS_CONTENT_URI, null)
            return cnt
        }

        URI_AGENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(AGENTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(AGENTS_CONTENT_URI, null)
            return cnt
        }

        URI_DISTRIBS_CONTRACTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(DISTRIBS_CONTRACTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver()
                .notifyChange(DISTRIBS_CONTRACTS_CONTENT_URI, null)
            return cnt
        }

        URI_DISTR_POINTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(DISTR_POINTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(DISTR_POINTS_CONTENT_URI, null)
            return cnt
        }

        URI_PRICETYPES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(PRICETYPES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(PRICETYPES_CONTENT_URI, null)
            return cnt
        }

        URI_PRICES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(PRICES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(PRICES_CONTENT_URI, null)
            return cnt
        }

        URI_PRICES_AGREEMENTS30 -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(PRICES_AGREEMENTS30_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver()
                .notifyChange(PRICES_AGREEMENTS30_CONTENT_URI, null)
            return cnt
        }

        URI_CLIENTS_PRICE -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(CLIENTS_PRICE_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(CLIENTS_PRICE_CONTENT_URI, null)
            return cnt
        }

        URI_CURATORS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(CURATORS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(CURATORS_CONTENT_URI, null)
            return cnt
        }

        URI_CURATORS_PRICE -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(CURATORS_PRICE_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(CURATORS_PRICE_CONTENT_URI, null)
            return cnt
        }

        URI_SIMPLE_DISCOUNTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(SIMPLE_DISCOUNTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(SIMPLE_DISCOUNTS_CONTENT_URI, null)
            return cnt
        }

        URI_ORDERS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
                getContext()!!.getContentResolver()
                    .notifyChange(ORDERS_JOURNAL_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
            }
            return cnt
        }

        URI_REFUNDS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
            }
            return cnt
        }

        URI_DISTRIBS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(DISTRIBS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
            }
            return cnt
        }

        URI_ORDERS_SILENT_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
            return cnt
        }

        URI_REFUNDS_SILENT_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
            return cnt
        }

        URI_ORDERS_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "order_id=?",
                arrayOf<String?>(_id),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
            }
            cursor.close()
            val cnt = db.delete(ORDERS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        ORDERS_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        ORDERS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                if (journal_id != 0L) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
            }
            return cnt
        }

        URI_REFUNDS_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "refund_id=?",
                arrayOf<String?>(_id),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
            }
            cursor.close()
            val cnt = db.delete(REFUNDS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        REFUNDS_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                if (journal_id != 0L) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
            }
            return cnt
        }

        URI_DISTRIBS_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "distribs_id=?",
                arrayOf<String?>(_id),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
            }
            cursor.close()
            val cnt = db.delete(DISTRIBS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        DISTRIBS_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                if (journal_id != 0L) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            JOURNAL_CONTENT_URI, journal_id
                        ), null
                    )
                }
            }
            return cnt
        }

        URI_ORDERS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ORDERS_LINES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ORDERS_LINES_CONTENT_URI, null)
            return cnt
        }

        URI_REFUNDS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(REFUNDS_LINES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(REFUNDS_LINES_CONTENT_URI, null)
            return cnt
        }

        URI_DISTRIBS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(DISTRIBS_LINES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(DISTRIBS_LINES_CONTENT_URI, null)
            return cnt
        }

        URI_ORDERS_PLACES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ORDERS_PLACES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ORDERS_PLACES_CONTENT_URI, null)
            return cnt
        }

        URI_CASH_PAYMENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver()
                    .notifyChange(CASH_PAYMENTS_CONTENT_URI, null)
                getContext()!!.getContentResolver().notifyChange(
                    CASH_PAYMENTS_JOURNAL_CONTENT_URI, null
                )
            }
            return cnt
        }

        URI_CASH_PAYMENTS_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val cnt = db.delete(CASH_PAYMENTS_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        CASH_PAYMENTS_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        CASH_PAYMENTS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
            }
            return cnt
        }

        URI_VERSIONS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(VERSIONS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(VERSIONS_CONTENT_URI, null)
            return cnt
        }

        URI_VERSIONS_SALES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(VERSIONS_SALES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(VERSIONS_SALES_CONTENT_URI, null)
            return cnt
        }

        URI_SALES_LOADED -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(SALES_LOADED_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(SALES_LOADED_CONTENT_URI, null)
            return cnt
        }

        URI_VICARIOUS_POWER -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(VICARIOUS_POWER_CONTENT_URI, null)
            return cnt
        }

        URI_VICARIOUS_POWER_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val cnt = db.delete(VICARIOUS_POWER_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        VICARIOUS_POWER_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
            }
            return cnt
        }

        URI_MESSAGES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(MESSAGES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null)
            getContext()!!.getContentResolver().notifyChange(MESSAGES_LIST_CONTENT_URI, null)
            return cnt
        }

        URI_MESSAGES_ID -> {
            val db = dbHelper!!.getWritableDatabase()
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val cnt = db.delete(MESSAGES_TABLE, where, selectionArgs)
            if (cnt > 0) {
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        MESSAGES_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        MESSAGES_LIST_CONTENT_URI, _id!!.toInt().toLong()
                    ), null
                )
            }
            return cnt
        }

        URI_GPS_COORD -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(GPS_COORD_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(GPS_COORD_CONTENT_URI, null)
            return cnt
        }

        URI_EQUIPMENT -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(EQUIPMENT_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(EQUIPMENT_CONTENT_URI, null)
            return cnt
        }

        URI_EQUIPMENT_RESTS -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(EQUIPMENT_RESTS_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(EQUIPMENT_RESTS_CONTENT_URI, null)
            return cnt
        }

        URI_ROUTES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ROUTES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ROUTES_CONTENT_URI, null)
            return cnt
        }

        URI_ROUTES_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ROUTES_LINES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ROUTES_LINES_CONTENT_URI, null)
            return cnt
        }

        URI_ROUTES_DATES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(ROUTES_DATES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver().notifyChange(ROUTES_DATES_CONTENT_URI, null)
            return cnt
        }

        URI_REAL_ROUTES_DATES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(REAL_ROUTES_DATES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver()
                .notifyChange(REAL_ROUTES_DATES_CONTENT_URI, null)
            return cnt
        }

        URI_REAL_ROUTES_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            val cnt = db.delete(REAL_ROUTES_LINES_TABLE, where, selectionArgs)
            getContext()!!.getContentResolver()
                .notifyChange(REAL_ROUTES_LINES_CONTENT_URI, null)
            return cnt
        }


        else -> throw IllegalArgumentException("Unknown URI (for delete)" + uri)
    }
}

override fun getType(uri: Uri): String? {
    return null
}

override fun insert(uri: Uri, values: ContentValues?): Uri? {
    Log.d(LOG_TAG, "insert, " + uri.toString())
    when (sUriMatcher.match(uri)) {
        URI_NOMENCLATURE -> {
            //Log.d(LOG_TAG, "URI_NOMENCLATURE");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(NOMENCLATURE_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                NOMENCLATURE_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(NOMENCLATURE_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_NOMENCLATURE_ID -> {
            val id = uri.getLastPathSegment()
            Log.d(LOG_TAG, "URI_NOMENCLATURE_ID, " + id)
            val db = dbHelper!!.getWritableDatabase()
            val rowID = db.insert(NOMENCLATURE_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_PLACES -> {
            //Log.d(LOG_TAG, "URI_NOMENCLATURE");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(PLACES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                PLACES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(PLACES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_OCCUPIED_PLACES -> {
            //Log.d(LOG_TAG, "URI_NOMENCLATURE");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "place_id=? and client_id=? and document_id=?"
            val selectionArgs = arrayOf<String?>(
                values!!.getAsString("place_id"),
                values!!.getAsString("client_id"),
                values!!.getAsString("document_id")
            )

            //Do an update if the constraints match
            db.update(OCCUPIED_PLACES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                OCCUPIED_PLACES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(OCCUPIED_PLACES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CLIENTS -> {
            Log.d(LOG_TAG, "URI_CLIENTS")
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(CLIENTS_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                CLIENTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CLIENTS_ID -> {
            val id = uri.getLastPathSegment()
            Log.d(LOG_TAG, "URI_CLIENTS_ID, " + id)
            val db = dbHelper!!.getWritableDatabase()
            val rowID = db.insert(CLIENTS_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(CLIENTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_RESTS -> {
            Log.d(LOG_TAG, "URI_RESTS")
            val db = dbHelper!!.getWritableDatabase()

            var selection = "nomenclature_id=? and stock_id=?"
            var selectionArgs = arrayOf<String?>(
                values!!.getAsString("nomenclature_id"),
                values.getAsString("stock_id")
            )

            // Тандем
            if (values.containsKey("ogranization_id")) {
                selection = "nomenclature_id=? and stock_id=? and ogranization_id=?"
                selectionArgs = arrayOf<String?>(
                    values.getAsString("nomenclature_id"),
                    values.getAsString("stock_id"),
                    values.getAsString("ogranization_id")
                )
            }

            //Do an update if the constraints match
            db.update(RESTS_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                RESTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(RESTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_SALDO -> {
            Log.d(LOG_TAG, "URI_SALDO")
            val db = dbHelper!!.getWritableDatabase()

            val selection = "client_id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("client_id"))

            //Do an update if the constraints match
            db.update(SALDO_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                SALDO_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(SALDO_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_SALDO_EXTENDED -> {
            Log.d(LOG_TAG, "URI_SALDO_EXTENDED")
            val db = dbHelper!!.getWritableDatabase()

            val selection =
                "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?"
            val selectionArgs = arrayOf<String?>(
                values!!.getAsString("client_id"),
                values.getAsString("agreement30_id"),
                values.getAsString("agreement_id"),
                values.getAsString("document_id"),
                values.getAsString("manager_id")
            )

            //Do an update if the constraints match
            db.update(SALDO_EXTENDED_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                SALDO_EXTENDED_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(SALDO_EXTENDED_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_VERSIONS -> {
            Log.d(LOG_TAG, "URI_VERSIONS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                VERSIONS_TABLE,
                values,
                "param=?",
                arrayOf<String?>(values!!.getAsString("param"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                VERSIONS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(VERSIONS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_VERSIONS_SALES -> {
            Log.d(LOG_TAG, "URI_VERSIONS_SALES")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                VERSIONS_SALES_TABLE,
                values,
                "param=?",
                arrayOf<String?>(values!!.getAsString("param"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                VERSIONS_SALES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(VERSIONS_SALES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_SEANCES_INCOMING -> {
            Log.d(LOG_TAG, "URI_SEANCES_INCOMING")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=ON")
            if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                db.delete(SEANCES_TABLE, null, null)
                db.insert(SEANCES_TABLE, null, values)
            }
            db.execSQL("PRAGMA synchronous=NORMAL")
            return null
        }

        URI_SEANCES_OUTGOING -> {
            Log.d(LOG_TAG, "URI_SEANCES_OUTGOING")
            val db = dbHelper!!.getWritableDatabase()
            if (db.update(SEANCES_TABLE, values, null, null) != 1) {
                db.delete(SEANCES_TABLE, null, null)
                db.insert(SEANCES_TABLE, null, values)
            }
            db.execSQL("PRAGMA synchronous=ON")
            return null
        }

        URI_REINDEX -> {
            Log.d(LOG_TAG, "URI_REINDEX")
            dbHelper!!.Reindex()
            return null
        }

        URI_VACUUM -> {
            Log.d(LOG_TAG, "URI_VACUUM")
            dbHelper!!.Vacuum()
            return null
        }


        URI_SORT -> {
            Log.d(LOG_TAG, "URI_SORT")
            val sortType = values!!.getAsString("sort_type")
            if (sortType == "orders_by_date") {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("drop table if exists temp_old_ids")
                db.execSQL(
                    "CREATE TABLE temp_old_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_old integer)"
                )
                db.execSQL("drop table if exists temp_new_ids")
                db.execSQL(
                    "CREATE TABLE temp_new_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_new integer)"
                )

                // сначала в первую таблицу записываем старые коды в старом порядке
                // distinct на случай, если дублируются коды order_id
                db.execSQL(
                    "insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by journal._id"
                )
                // а потом во вторую - в новом порядке
                db.execSQL(
                    "insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by ifnull(orders.datedoc, refunds.datedoc);"
                )
                // количество записей будет совпадать
                //db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);");
                db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);")
                db.execSQL("UPDATE journal set _id = -_id")

                db.execSQL("drop table if exists temp_old_ids")
                db.execSQL("drop table if exists temp_new_ids")
            } else if (sortType == "orders_by_service_date") {
                val db = dbHelper!!.getWritableDatabase()
                db.execSQL("drop table if exists temp_old_ids")
                db.execSQL(
                    "CREATE TABLE temp_old_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_old integer)"
                )
                db.execSQL("drop table if exists temp_new_ids")
                db.execSQL(
                    "CREATE TABLE temp_new_ids (" +
                            "_id integer primary key autoincrement," +
                            "_id_new integer)"
                )

                // сначала в первую таблицу записываем старые коды в старом порядке
                // distinct на случай, если дублируются коды order_id
                db.execSQL(
                    "insert into temp_old_ids(_id_old) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by journal._id desc"
                )
                // а потом во вторую - в новом порядке
                db.execSQL(
                    "insert into temp_new_ids(_id_new) select distinct journal._id from journal " +
                            "left join orders on orders._id=journal.order_id " +
                            "left join refunds on refunds._id=journal.refund_id " +
                            "order by ifnull(orders.shipping_date, refunds.datedoc) desc;"
                )
                // количество записей будет совпадать
                //db.execSQL("UPDATE journal set _id = -(select _id_new from temp_new_ids join temp_old_ids on temp_old_ids._id=temp_new_ids._id where temp_old_ids._id_old=journal._id);");
                db.execSQL("UPDATE journal set _id = -(select _id_old from temp_new_ids join temp_old_ids on temp_new_ids._id=temp_old_ids._id where temp_new_ids._id_new=journal._id);")
                db.execSQL("UPDATE journal set _id = -_id")

                db.execSQL("drop table if exists temp_old_ids")
                db.execSQL("drop table if exists temp_new_ids")
            }
            getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
            getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)


            return null
        }


        URI_CREATE_VIEWS -> {
            // TODO отбор по торговым точкам
            // а вообще salesVB нигде не используется уже
            // вместо него rests_sales_stuff
            Log.d(LOG_TAG, "URI_CREATE_VIEWS")
            val db = dbHelper!!.getWritableDatabase()
            // Продажи в телефоне (текущие)
            db.execSQL("drop view if exists salesVB")
            db.execSQL(
                "CREATE VIEW salesVB as select " +
                        "nomenclature_id, " +
                        "sum(quantity) as quantity_saled from orders " +
                        "join ordersLines on ordersLines.order_id=orders._id and orders.client_id=\"" + values!!.getAsString(
                    "client_id"
                ) + "\" " +
                        "where orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                        "group by nomenclature_id;"
            )

            // Продажи в 1С (история)
            db.execSQL("drop view if exists salesV")

            // То же самое, для окна ввода количества
            //db.execSQL("drop view if exists salesGroupsV");
            /*
        db.execSQL("CREATE VIEW salesV as select " +
                "nomenclature_id,"+
                "sum(quantity) as quantity_saled "+
                "from salesloaded "+
                "where client_id=\""+values.getAsString("client_id")+"\" "+
                "group by nomenclature_id;");
        */
            db.execSQL(
                "CREATE VIEW salesV as select " +
                        "nomenclature_id," +
                        "strQuantity " +
                        "from salesL " +
                        "where client_id=\"" + values!!.getAsString("client_id") + "\" " +
                        ";"
            )

            db.execSQL("drop view if exists salesV_7")

            db.execSQL(
                "CREATE VIEW salesV_7 as " +
                        "select " +
                        "nomenclature_id," +
                        "sum(case when datedoc between '" + values!!.getAsString("dt1b") + "' and '" + values!!.getAsString(
                    "dt1e"
                ) + "' then quantity else 0 end) quantity7_1," +
                        "sum(case when datedoc between '" + values.getAsString("dt2b") + "' and '" + values.getAsString(
                    "dt2e"
                ) + "' then quantity else 0 end) quantity7_2," +
                        "sum(case when datedoc between '" + values.getAsString("dt3b") + "' and '" + values.getAsString(
                    "dt3e"
                ) + "' then quantity else 0 end) quantity7_3," +
                        "sum(case when datedoc between '" + values.getAsString("dt4b") + "' and '" + values.getAsString(
                    "dt4e"
                ) + "' then quantity else 0 end) quantity7_4 " +
                        "from " +
                        "(" +
                        "select " +
                        "nomenclature_id," +
                        "datedoc," +
                        "case when ifnull(sum(quantity_server), 0)>=ifnull(sum(quantity_pda), 0) then ifnull(sum(quantity_server), 0) else sum(quantity_pda) end quantity " +
                        "from" +
                        "(" +
                        "select nomenclature_id, substr(datedoc, 1, 8) datedoc, quantity quantity_server, 0 quantity_pda from salesL2 " +  // Оставим только группы
                        "join nomenclature n on n.id=nomenclature_id and n.isFolder=1 " +
                        "where client_id='" + values.getAsString("client_id") + "' and datedoc between '" + values.getAsString(
                    "dtb"
                ) + "' and '" + values.getAsString("dte") + "' " +
                        "union all " +
                        "select n.parent_id nomenclature_id, substr(datedoc, 1, 8) datedoc, 0 quantity_server, quantity quantity_pda from orders " +
                        "join ordersLines on ordersLines.order_id=orders._id " +
                        "join nomenclature n on n.id=ordersLines.nomenclature_id " +
                        "where datedoc between '" + values.getAsString("dtb") + "' and '" + values.getAsString(
                    "dte"
                ) + "' and orders.client_id='" + values.getAsString("client_id") + "' " +
                        "and orders.state=" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + " " +
                        ") sales0 " +
                        "group by nomenclature_id, datedoc" +
                        ") sales " +
                        "group by nomenclature_id" +
                        ";"
            )

            return null
        }

        URI_CREATE_SALES_L -> {
            Log.d(LOG_TAG, "URI_RECALC_SALES_IN_LINE")
            val db = dbHelper!!.getWritableDatabase()

            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()

            db.execSQL("delete from salesL")
            db.execSQL("delete from salesL2")

            // Заполнение salesL
            //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, distr_point_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, distr_point_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
            val cursor = db.rawQuery(
                "select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc",
                null
            )
            // '    45   '
            //Cursor cursor=db.rawQuery("select case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, client_id, datedoc, sum(quantity) as quantity from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id where s.nomenclature_id='    45   ' group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, client_id, datedoc order by nomenclature_id, client_id, datedoc desc", null);
            //
            val nomenclatureIdIndex = cursor.getColumnIndex("nomenclature_id")
            val clientIdIndex = cursor.getColumnIndex("client_id")
            //int distrPointIdIndex=cursor.getColumnIndex("distr_point_id");
            //int datedocIndex=cursor.getColumnIndex("datedoc");
            val quantityIndex = cursor.getColumnIndex("quantity")
            var prevNomenclatureId = ""
            var prevClientId = ""
            //String prevDistrPointId="";
            var strQuantity = ""
            var cnt = 0
            var quantityTotal = 0.0

            try {
                while (cursor.moveToNext()) {
                    val nomenclatureId = cursor.getString(nomenclatureIdIndex)
                    val clientId = cursor.getString(clientIdIndex)
                    //String distrPointId=cursor.getString(distrPointIdIndex);
                    //String datedoc=cursor.getString(datedocIndex);
                    val quantity = cursor.getDouble(quantityIndex)
                    //if (!prevNomenclatureId.equals(nomenclatureId)||!(prevClientId.equals(clientId))||!(prevDistrPointId.equals(distrPointId)))
                    if (prevNomenclatureId != nomenclatureId || !(prevClientId == clientId)) {
                        if (quantityTotal > 0.001) {
                            val cv = ContentValues()
                            cv.put("nomenclature_id", prevNomenclatureId)
                            cv.put("client_id", prevClientId)
                            //cv.put("distr_point_id", prevDistrPointId);
                            cv.put("strQuantity", strQuantity)
                            cv.put("quantity", quantityTotal)

                            //cv.put("quantity_now", 0.0);

                            //db.insert(SALES_L_TABLE, null, cv);
                            db.insertWithOnConflict(
                                SALES_L_TABLE,
                                null,
                                cv,
                                SQLiteDatabase.CONFLICT_IGNORE
                            )
                        }

                        prevNomenclatureId = nomenclatureId
                        prevClientId = clientId

                        //prevDistrPointId=distrPointId;
                        quantityTotal = 0.0
                        strQuantity = ""
                        cnt = 0
                    }
                    if (quantity > 0.001) {
                        quantityTotal += quantity
                        if (strQuantity.isEmpty()) {
                            strQuantity = Common.DoubleToStringFormat(quantity, "%.3f")
                        } else {
                            cnt++
                            if (cnt == 4) strQuantity += "+"
                            else if (cnt < 4) strQuantity += "+" + Common.DoubleToStringFormat(
                                quantity,
                                "%.3f"
                            )
                        }
                    }
                }

                if (quantityTotal > 0.001) {
                    val cv = ContentValues()
                    cv.put("nomenclature_id", prevNomenclatureId)
                    cv.put("client_id", prevClientId)
                    //cv.put("distr_point_id", prevDistrPointId);
                    cv.put("quantity", quantityTotal)
                    cv.put("strQuantity", strQuantity)

                    //db.insert(SALES_L_TABLE, null, cv);
                    db.insertWithOnConflict(
                        SALES_L_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
            } catch (e: SQLException) {
                Log.e(LOG_TAG, e.toString())
            } finally {
                db.endTransaction()
            }
            cursor.close()

            // Заполнение salesL2
            // если торговые точки не используются, то везде будет одно и то же значение, и группировка от этого не пострадает
            db.execSQL(
                "insert into salesL2 select " +
                        "null as _id," +
                        "case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id," +
                        "client_id," +  // 20.09.2019
                        "null as distr_point_id," +  //
                        "datedoc," +
                        "numdoc," +
                        "sum(quantity) as quantity," +
                        "sum(price) as price " +
                        "from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id " +
                        "group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, distr_point_id;"
            )

            return null
        }

        URI_PRICETYPES -> {
            Log.d(LOG_TAG, "URI_PRICETYPES")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                PRICETYPES_TABLE,
                values,
                "id=?",
                arrayOf<String?>(values!!.getAsString("id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                PRICETYPES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(PRICETYPES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_AGREEMENTS -> {
            Log.d(LOG_TAG, "URI_AGREEMENTS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                AGREEMENTS_TABLE,
                values,
                "id=?",
                arrayOf<String?>(values!!.getAsString("id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                AGREEMENTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(AGREEMENTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_AGREEMENTS30 -> {
            Log.d(LOG_TAG, "URI_AGREEMENTS30")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                AGREEMENTS30_TABLE,
                values,
                "id=? and owner_id=?",
                arrayOf<String?>(values!!.getAsString("id"), values.getAsString("owner_id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                AGREEMENTS30_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(AGREEMENTS30_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_STOCKS -> {
            Log.d(LOG_TAG, "URI_STOCKS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(STOCKS_TABLE, values, "id=?", arrayOf<String?>(values!!.getAsString("id")))

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                STOCKS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(STOCKS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_PRICES -> {
            Log.d(LOG_TAG, "URI_PRICES")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                PRICES_TABLE,
                values,
                "nomenclature_id=? and price_type_id=?",
                arrayOf<String?>(
                    values!!.getAsString("nomenclature_id"),
                    values.getAsString("price_type_id")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                PRICES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(PRICES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_PRICES_AGREEMENTS30 -> {
            Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                PRICES_AGREEMENTS30_TABLE,
                values,
                "agreement30_id=? and nomenclature_id=?",
                arrayOf<String?>(
                    values!!.getAsString("agreement30_id"),
                    values.getAsString("nomenclature_id")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                PRICES_AGREEMENTS30_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(PRICES_AGREEMENTS30_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CLIENTS_PRICE -> {
            Log.d(LOG_TAG, "URI_CLIENTS_PRICE")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                CLIENTS_PRICE_TABLE,
                values,
                "client_id=? and nomenclature_id=?",
                arrayOf<String?>(
                    values!!.getAsString("client_id"),
                    values.getAsString("nomenclature_id")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                CLIENTS_PRICE_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(CLIENTS_PRICE_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CURATORS_PRICE -> {
            Log.d(LOG_TAG, "URI_CURATORS_PRICE")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                CURATORS_PRICE_TABLE,
                values,
                "curator_id=? and nomenclature_id=?",
                arrayOf<String?>(
                    values!!.getAsString("curator_id"),
                    values.getAsString("nomenclature_id")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                CURATORS_PRICE_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(CURATORS_PRICE_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_SIMPLE_DISCOUNTS -> {
            Log.d(LOG_TAG, "URI_SIMPLE_DISCOUNTS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                SIMPLE_DISCOUNTS_TABLE,
                values,
                "id=?",
                arrayOf<String?>(values!!.getAsString("id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                SIMPLE_DISCOUNTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(SIMPLE_DISCOUNTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_AGENTS -> {
            Log.d(LOG_TAG, "URI_AGENTS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(AGENTS_TABLE, values, "id=?", arrayOf<String?>(values!!.getAsString("id")))

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                AGENTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(AGENTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CURATORS -> {
            Log.d(LOG_TAG, "URI_CURATORS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                CURATORS_TABLE,
                values,
                "id=?",
                arrayOf<String?>(values!!.getAsString("id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                CURATORS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(CURATORS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_DISTR_POINTS -> {
            Log.d(LOG_TAG, "URI_DISTR_POINTS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                DISTR_POINTS_TABLE,
                values,
                "id=? and owner_id=?",
                arrayOf<String?>(values!!.getAsString("id"), values.getAsString("owner_id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                DISTR_POINTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(DISTR_POINTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_ORGANIZATIONS -> {
            Log.d(LOG_TAG, "URI_ORGANIZATIONS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                ORGANIZATIONS_TABLE,
                values,
                "id=?",
                arrayOf<String?>(values!!.getAsString("id"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                ORGANIZATIONS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(ORGANIZATIONS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_ORDERS_SILENT -> {
            Log.d(LOG_TAG, "URI_ORDERS_SILENT")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            // с 19.02.2018
            db.update(
                ORDERS_TABLE,
                values,
                "uid=? and editing_backup=?",
                arrayOf<String?>(
                    values!!.getAsString("uid"),
                    values.getAsString("editing_backup")
                )
            )

            // до
            //db.update(ORDERS_TABLE, values, "uid=?", new String[] {values.getAsString("uid")});

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                ORDERS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID)

            return resultUri
        }

        URI_ORDERS -> {
            Log.d(LOG_TAG, "URI_ORDERS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                ORDERS_TABLE,
                values,
                "uid=? and editing_backup=?",
                arrayOf<String?>(
                    values!!.getAsString("uid"),
                    values.getAsString("editing_backup")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                ORDERS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            var resultUri = ContentUris.withAppendedId(ORDERS_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri = ContentUris.withAppendedId(ORDERS_JOURNAL_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "order_id=?",
                arrayOf<String>(rowID.toString()),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        JOURNAL_CONTENT_URI, journal_id
                    ), null
                )
            }
            cursor.close()
            //db.close();
            return resultUri
        }

        URI_ORDERS_LINES -> {
            Log.d(LOG_TAG, "URI_ORDERS_LINES")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(ORDERS_LINES_TABLE, null, values)

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            var resultUri = ContentUris.withAppendedId(ORDERS_LINES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri = ContentUris.withAppendedId(ORDERS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }


        URI_REFUNDS_SILENT -> {
            Log.d(LOG_TAG, "URI_REFUNDS_SILENT")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                REFUNDS_TABLE,
                values,
                "uid=?",
                arrayOf<String?>(values!!.getAsString("uid"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                REFUNDS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID)

            return resultUri
        }

        URI_REFUNDS -> {
            Log.d(LOG_TAG, "URI_REFUNDS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                REFUNDS_TABLE,
                values,
                "uid=?",
                arrayOf<String?>(values!!.getAsString("uid"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                REFUNDS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            val resultUri = ContentUris.withAppendedId(REFUNDS_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "refund_id=?",
                arrayOf<String>(rowID.toString()),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        JOURNAL_CONTENT_URI, journal_id
                    ), null
                )
            }
            cursor.close()
            //db.close();
            return resultUri
        }

        URI_REFUNDS_LINES -> {
            Log.d(LOG_TAG, "URI_REFUNDS_LINES")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(REFUNDS_LINES_TABLE, null, values)

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            var resultUri = ContentUris.withAppendedId(REFUNDS_LINES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri =
                ContentUris.withAppendedId(REFUNDS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_DISTRIBS_LINES -> {
            Log.d(LOG_TAG, "URI_DISTRIBS_LINES")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(DISTRIBS_LINES_TABLE, null, values)

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            var resultUri = ContentUris.withAppendedId(DISTRIBS_LINES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri =
                ContentUris.withAppendedId(DISTRIBS_LINES_COMPLEMENTED_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_CASH_PAYMENTS -> {
            Log.d(LOG_TAG, "URI_CASH_PAYMENTS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                CASH_PAYMENTS_TABLE,
                values,
                "uid=?",
                arrayOf<String?>(values!!.getAsString("uid"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                CASH_PAYMENTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            var resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri = ContentUris.withAppendedId(CASH_PAYMENTS_JOURNAL_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_VICARIOUS_POWER -> {
            //Log.d(LOG_TAG, "URI_MESSAGES");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(VICARIOUS_POWER_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                VICARIOUS_POWER_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(VICARIOUS_POWER_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_MESSAGES -> {
            //Log.d(LOG_TAG, "URI_MESSAGES");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "uid=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("uid"))

            //Do an update if the constraints match
            db.update(MESSAGES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                MESSAGES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            var resultUri = ContentUris.withAppendedId(MESSAGES_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            resultUri = ContentUris.withAppendedId(MESSAGES_LIST_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_SALES -> {
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("drop table if exists sales;")
            // Продажи каждому клиенту
            db.execSQL(
                "create table sales as " +
                        "select ordersLines.client_id as client_id, nomenclature_id, sum(quantity) as quantity_saled from ordersLines join orders on orders._id=ordersLines.order_id and datedoc>=? group by ordersLines.client_id, nomenclature_id" +
                        ";", arrayOf<String?>(values!!.getAsString("datebegin"))
            )
            // Общие продажи (клиент=null)
            db.execSQL(
                "insert into sales " +
                        "select null as client_id, nomenclature_id, sum(quantity_saled) as quantity_saled from sales group by nomenclature_id" +
                        ";"
            )

            /*
        if (values.containsKey("datebegin"))
        {

        }
        */
            // insert into xxx
            // select * from yyyy;
            return null
        }

        URI_RESTS_SALES_STUFF -> {
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("DROP TABLE IF EXISTS rests_sales_stuff;")

            //db.execSQL("CREATE TEMPORARY TABLE temp_Nomenclature as select * from nomenclature");
            //db.execSQL("INSERT INTO RestsSalesStuff select * from temp_Nomenclature");
            db.execSQL(
                "CREATE TABLE rests_sales_stuff AS " +
                        "SELECT nomenclature_id, sum(quantity) as quantity, sum(quantity_reserve) as quantity_reserve, sum(saled) as saledNow " +
                        "FROM ( " +
                        "SELECT nomenclature_id, quantity, quantity_reserve, 0 as saled " +
                        "FROM rests " + (if (values!!.containsKey("stock_id")) "where stock_id=\"" + values.getAsString(
                    "stock_id"
                ) + "\" " else "") +  // У тандема разделение остатков по организациям
                        // проверяется, есть ли stock_id, чтобы написать where или and
                        (if (values.containsKey("organization_id") && !values.containsKey("stock_id")) " where organization_id=\"" + values.getAsString(
                            "organization_id"
                        ) + "\" " else "") +
                        (if (values.containsKey("organization_id") && values.containsKey("stock_id")) " and organization_id=\"" + values.getAsString(
                            "organization_id"
                        ) + "\" " else "") +  //
                        (if (values.containsKey("client_id")) ("UNION ALL " +
                                "SELECT nomenclature_id, 0, 0, quantity as saled " +
                                "FROM orders join ordersLines on ordersLines.order_id=orders._id " +
                                "WHERE orders.client_id=\"" + values.getAsString("client_id") + "\" " +
                                (if (values.containsKey("distr_point_id")) " and distr_point_id=\"" + values.getAsString(
                                    "distr_point_id"
                                ) + "\" " else "") +  // TODO остальные состояния
                                "and orders.state in (" + E_ORDER_STATE.E_ORDER_STATE_COMPLETED.value() + ") ") else "") +  //
                        ") in1 " +
                        "GROUP BY nomenclature_id"
            )

            db.execSQL("CREATE INDEX sales_nomenclature_id ON rests_sales_stuff (nomenclature_id ASC);")

            return null
        }

        URI_DISCOUNTS_STUFF_MEGA, URI_DISCOUNTS_STUFF_SIMPLE, URI_DISCOUNTS_STUFF_OTHER -> {
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("DROP TABLE IF EXISTS discounts_stuff;")
            db.execSQL("DROP TABLE IF EXISTS temp_discounts_stuff;")

            //"_id integer primary key autoincrement, " +
            //"client_id text," +
            //"nomenclature_id text," +
            //"priceProcent double," +
            //"priceAdd double," +

            // добавлено 26.02.2018
            var bFastMode = false

            //
            if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_MEGA) {
                db.execSQL(
                    "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +  // добавляем группы
                            "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                            "from nomenclature_hierarchy h join curators_price p on " + (if (values!!.containsKey(
                            "curator_id"
                        )
                    ) "curator_id=\"" + values.getAsString("curator_id") + "\" " else "0=1 ") +
                            "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +  // и номенклатуру отдельно, уровень ставим 100
                            "union all " +
                            "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                            "from nomenclature_hierarchy h join curators_price p on " + (if (values.containsKey(
                            "curator_id"
                        )
                    ) "curator_id=\"" + values.getAsString("curator_id") + "\" " else "0=1 ") +
                            "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                            ";"
                )
            } else  //if (g.Common.TITAN||g.Common.PHARAOH||g.Common.ISTART)
                if (sUriMatcher.match(uri) == URI_DISCOUNTS_STUFF_SIMPLE) {
                    // Тут простейший (обычный для 1С) вариант, скидка от номенклатуры не зависит
                    // а только от договора, но номенклатуру все равно выберем
                    // будет в итоге при работе соединение с той же самой таблицей
                    // группы нам здесь не нужны, только элементы
                    db.execSQL(
                        "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +
                                "select _id p_id, id nomenclature_id, 100 level, " + values!!.getAsString(
                            "priceProcent"
                        ) + " priceProcent, 0 priceAdd " +
                                "from nomenclature where isFolder<>1 " +
                                ";"
                    )

                    bFastMode = true
                } else {
                    db.execSQL(
                        "CREATE TEMPORARY TABLE temp_discounts_stuff AS " +  // добавляем группы
                                "select p._id p_id,  h.id nomenclature_id, level, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join clients_price p on " + (if (values!!.containsKey(
                                "client_id"
                            )
                        ) "client_id=\"" + values!!.getAsString("client_id") + "\" " else "0=1 ") +
                                "and p.nomenclature_id in (level0_id, level1_id, level2_id, level3_id, level4_id, level5_id, level6_id, level7_id, level8_id) " +  // и номенклатуру отдельно, уровень ставим 100
                                "union all " +
                                "select p._id, nomenclature_id, 100, priceProcent, priceAdd " +
                                "from nomenclature_hierarchy h join clients_price p on " + (if (values.containsKey(
                                "client_id"
                            )
                        ) "client_id=\"" + values.getAsString("client_id") + "\" " else "0=1 ") +
                                "join nomenclature on nomenclature_id=nomenclature.id and isFolder<>1 " +
                                ";"
                    )
                }

            if (bFastMode) {
                // Просто копируем эту таблицу
                db.execSQL(
                    "CREATE TABLE discounts_stuff AS " +
                            "select nomenclature_id, p_id, level, priceProcent, priceAdd from temp_discounts_stuff;"
                )
            } else {
                // заполним level максимальным совпавшим значением для номенклатуры, id - чтобы появилась колонка
                db.execSQL(
                    "CREATE TABLE discounts_stuff AS " +
                            "select nomenclature_id, max(p_id) p_id, max(level) level, 0.0 as priceProcent, 0.0 as priceAdd from temp_discounts_stuff group by nomenclature_id;"
                )
                // находим _id из clients_prict
                db.execSQL("UPDATE discounts_stuff set p_id = (select p_id from temp_discounts_stuff t2 where t2.nomenclature_id=discounts_stuff.nomenclature_id and t2.level=discounts_stuff.level);")
                // и все остальное
                db.execSQL(
                    "UPDATE discounts_stuff set " +
                            "priceProcent = (select priceProcent from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id)," +
                            "priceAdd = (select priceAdd from temp_discounts_stuff t2 where t2.p_id=discounts_stuff.p_id);"
                )
            }

            db.execSQL("CREATE INDEX discounts_nomenclature_id ON discounts_stuff (nomenclature_id ASC);")

            return null
        }

        URI_PRICESV_MEGA, URI_PRICESV_OTHER -> {
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("DROP VIEW IF EXISTS PricesV;")
            //if (Common.MEGA)
            if (sUriMatcher.match(uri) == URI_PRICESV_MEGA) {
                var priceName = "rozn_price"
                val price_type_id = values!!.getAsString("price_type_id")
                if (price_type_id == E_PRICE_TYPE.E_PRICE_TYPE_M_OPT.value().toString()) {
                    priceName = "m_opt_price"
                } else if (price_type_id == E_PRICE_TYPE.E_PRICE_TYPE_OPT.value().toString()) {
                    priceName = "opt_price"
                }
                db.execSQL(
                    "CREATE VIEW PricesV as select " +
                            "id as nomenclature_id," +
                            "edizm_1_id as ed_izm_id," +
                            "quant_1 as edIzm," +
                            priceName + " as price," +
                            "0 as priceProcent," +
                            "quant_k_1 as k " +
                            "from nomenclature " +
                            ";"
                )
            } else  /*
        if (Common.TITAN)
        {
            // Тут простейший (обычный для 1С) вариант, скидка от номенклатуры не зависит
            // а только от договора, но номенклатуру все равно выберем
            // будет в итоге при работе соединение с той же самой таблицей
            db.execSQL("CREATE VIEW PricesV as select " +
                    "nomenclature_id,"+
                    "ed_izm_id,"+
                    "edIzm,"+
                    "price,"+
                    "priceProcent,"+
                    "k "+
                    "from prices where price_type_id=\""+values.getAsString("price_type_id")+"\""+
                    ";");
        } else
        */
                if (values!!.containsKey("agreement30_id")) {
                    // это Common.FACTORY
                    db.execSQL(
                        "CREATE VIEW PricesV as select " +
                                "ifnull(pa30.nomenclature_id, prices.nomenclature_id) nomenclature_id," +
                                "ifnull(pa30.ed_izm_id, prices.ed_izm_id) ed_izm_id," +
                                "ifnull(pa30.edIzm, prices.edIzm) edIzm," +
                                "ifnull(pa30.price, prices.price) price," +
                                "priceProcent," +  // на самом деле это лишнее поле, оно не используется
                                "ifnull(pa30.k, prices.k) k " +
                                "from prices" +  // вообще тут нужен FULL JOIN, но он не поддерживается
                                " left join prices_agreements30 pa30 on pa30.agreement30_id=\"" + values.getAsString(
                            "agreement30_id"
                        ) + "\"" +
                                " and pa30.nomenclature_id=prices.nomenclature_id" +
                                " where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                ";"
                    )
                } else {
                    db.execSQL(
                        "CREATE VIEW PricesV as select " +
                                "nomenclature_id," +
                                "ed_izm_id," +
                                "edIzm," +
                                "price," +
                                "priceProcent," +
                                "k " +
                                "from prices where price_type_id=\"" + values.getAsString("price_type_id") + "\"" +
                                ";"
                    )
                }
            return null
        }

        URI_SETTINGS -> {
            Log.d(LOG_TAG, "URI_SETTINGS")
            val db = dbHelper!!.getWritableDatabase()

            val cnt = db.update(SETTINGS_TABLE, values, null, null)
            if (cnt <= 0) {
                db.insert(SETTINGS_TABLE, null, values)
            }
            return null
        }

        URI_SALES_LOADED -> {
            Log.d(LOG_TAG, "URI_SALES_LOADED")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                SALES_LOADED_TABLE,
                values,
                "nomenclature_id=? and refdoc=?",
                arrayOf<String?>(
                    values!!.getAsString("nomenclature_id"),
                    values.getAsString("refdoc")
                )
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                SALES_LOADED_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(SALES_LOADED_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_NOMENCLATURE_HIERARCHY -> {
            Log.d(LOG_TAG, "NOMENCLATURE_HIERARCHY")
            val db = dbHelper!!.getWritableDatabase()
            val rowID = db.insertWithOnConflict(
                NOMENCLATURE_HIERARCHY_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )
            val resultUri =
                ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            return resultUri
        }

        URI_GPS_COORD -> {
            Log.d(LOG_TAG, "URI_GPS_COORD")
            val db = dbHelper!!.getWritableDatabase()

            // вообще можно просто добавлять, дублирования кода не будет
            //Do an update if the constraints match
            db.update(
                GPS_COORD_TABLE,
                values,
                "datecoord=?",
                arrayOf<String?>(values!!.getAsString("datecoord"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                GPS_COORD_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            return null
        }

        URI_DISTRIBS_CONTRACTS -> {
            //Log.d(LOG_TAG, "URI_DISTRIBS_CONTRACTS");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(DISTRIBS_CONTRACTS_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                DISTRIBS_CONTRACTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(DISTRIBS_CONTRACTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_DISTRIBS -> {
            Log.d(LOG_TAG, "URI_DISTRIBS")
            val db = dbHelper!!.getWritableDatabase()

            //Do an update if the constraints match
            db.update(
                DISTRIBS_TABLE,
                values,
                "uid=?",
                arrayOf<String?>(values!!.getAsString("uid"))
            )

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                DISTRIBS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            val resultUri = ContentUris.withAppendedId(DISTRIBS_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            // получим _id журнала
            var journal_id: Long = 0
            val cursor = db.query(
                JOURNAL_TABLE,
                arrayOf<String>("journal._id"),
                "distribs_id=?",
                arrayOf<String>(rowID.toString()),
                null,
                null,
                null
            )
            if (cursor.moveToNext()) {
                journal_id = cursor.getLong(0)
                getContext()!!.getContentResolver().notifyChange(
                    ContentUris.withAppendedId(
                        JOURNAL_CONTENT_URI, journal_id
                    ), null
                )
            }
            cursor.close()
            //db.close();
            return resultUri
        }

        URI_EQUIPMENT -> {
            //Log.d(LOG_TAG, "URI_EQUIPMENT");
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(EQUIPMENT_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                EQUIPMENT_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(EQUIPMENT_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_EQUIPMENT_RESTS -> {
            Log.d(LOG_TAG, "URI_EQUIPMENT_RESTS")
            val db = dbHelper!!.getWritableDatabase()

            val selection =
                "client_id=? and agreement_id=? and nomenclature_id=? and distr_point_id=? and doc_id=?"
            val selectionArgs = arrayOf<String?>(
                values!!.getAsString("client_id"),
                values.getAsString("agreement_id"),
                values.getAsString("nomenclature_id"),
                values.getAsString("distr_point_id"),
                values.getAsString("doc_id")
            )

            //Do an update if the constraints match
            db.update(EQUIPMENT_RESTS_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                EQUIPMENT_RESTS_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(EQUIPMENT_RESTS_CONTENT_URI, rowID)
            // уведомляем ContentResolver, что данные по адресу resultUri изменились
            getContext()!!.getContentResolver().notifyChange(resultUri, null)
            //db.close();
            return resultUri
        }

        URI_MTRADE_LOG -> {
            Log.d(LOG_TAG, "URI_MTRADE_LOG")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(MTRADE_LOG_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(MTRADE_LOG_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_PERMISSIONS_REQUESTS -> {
            Log.d(LOG_TAG, "URI_PERMISSIONS_REQUESTS")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(PERMISSIONS_REQUESTS_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(PERMISSIONS_REQUESTS_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_ROUTES -> {
            Log.d(LOG_TAG, "URI_ROUTES")
            val db = dbHelper!!.getWritableDatabase()

            val selection = "id=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("id"))

            //Do an update if the constraints match
            db.update(ROUTES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                ROUTES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(ROUTES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_ROUTES_LINES -> {
            Log.d(LOG_TAG, "URI_ROUTES_LINES")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(ROUTES_LINES_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(ROUTES_LINES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_ROUTES_DATES -> {
            Log.d(LOG_TAG, "URI_ROUTES_DATES")
            val db = dbHelper!!.getWritableDatabase()

            val selection = "route_date=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("route_date"))

            //Do an update if the constraints match
            db.update(ROUTES_DATES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                ROUTES_DATES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(ROUTES_DATES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_REAL_ROUTES_DATES -> {
            Log.d(LOG_TAG, "URI_ROUTES_DATES")
            val db = dbHelper!!.getWritableDatabase()

            val selection = "route_date=?"
            val selectionArgs = arrayOf<String?>(values!!.getAsString("route_date"))

            //Do an update if the constraints match
            db.update(REAL_ROUTES_DATES_TABLE, values, selection, selectionArgs)

            //This will return the id of the newly inserted row if no conflict
            //It will also return the offending row without modifying it if in conflict
            val rowID = db.insertWithOnConflict(
                REAL_ROUTES_DATES_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
            )

            val resultUri = ContentUris.withAppendedId(REAL_ROUTES_DATES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        URI_REAL_ROUTES_LINES -> {
            Log.d(LOG_TAG, "URI_ROUTES_LINES")
            val db = dbHelper!!.getWritableDatabase()

            val rowID = db.insert(REAL_ROUTES_LINES_TABLE, null, values)
            val resultUri = ContentUris.withAppendedId(REAL_ROUTES_LINES_CONTENT_URI, rowID)
            getContext()!!.getContentResolver().notifyChange(resultUri, null)

            return resultUri
        }

        else -> throw IllegalArgumentException("Unknown URI (for insert) " + uri)
    }
}

override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
    var numInserted = 0

    when (sUriMatcher.match(uri)) {
        URI_PRICES -> {
            Log.d(LOG_TAG, "URI_PRICES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        PRICES_TABLE,
                        cv,
                        "nomenclature_id=? and price_type_id=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("price_type_id")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        PRICES_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_PRICES_AGREEMENTS30 -> {
            Log.d(LOG_TAG, "URI_PRICES_AGREEMENTS30_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        PRICES_AGREEMENTS30_TABLE,
                        cv,
                        "agreement30_id=? and nomenclature_id=?",
                        arrayOf<String?>(
                            cv.getAsString("agreement30_id"),
                            cv.getAsString("nomenclature_id")
                        )
                    )

                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        PRICES_AGREEMENTS30_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )

                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_SALDO -> {
            Log.d(LOG_TAG, "URI_SALDO_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        SALDO_TABLE,
                        cv,
                        "client_id=?",
                        arrayOf<String?>(cv.getAsString("client_id"))
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        SALDO_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_SALDO_EXTENDED -> {
            Log.d(LOG_TAG, "URI_SALDO_EXTENDED_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        SALDO_EXTENDED_TABLE,
                        cv,
                        "client_id=? and agreement30_id=? and agreement_id=? and document_id=? and manager_id=?",
                        arrayOf<String?>(
                            cv.getAsString("client_id"),
                            cv.getAsString("agreement30_id"),
                            cv.getAsString("agreement_id"),
                            cv.getAsString("document_id"),
                            cv.getAsString("manager_id")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        SALDO_EXTENDED_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_AGREEMENTS -> {
            Log.d(LOG_TAG, "URI_AGREEMENTS_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        AGREEMENTS_TABLE,
                        cv,
                        "id=?",
                        arrayOf<String?>(cv.getAsString("id"))
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        AGREEMENTS_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_AGREEMENTS30 -> {
            Log.d(LOG_TAG, "URI_AGREEMENTS30_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        AGREEMENTS30_TABLE,
                        cv,
                        "id=? and owner_id=?",
                        arrayOf<String?>(cv.getAsString("id"), cv.getAsString("owner_id"))
                    )

                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        AGREEMENTS30_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )

                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_RESTS -> {
            Log.d(LOG_TAG, "URI_RESTS_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    // Тандем
                    if (cv.containsKey("organization_id")) db.update(
                        RESTS_TABLE,
                        cv,
                        "nomenclature_id=? and stock_id=? and organization_id=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("stock_id"),
                            cv.getAsString("organization_id")
                        )
                    )
                    else db.update(
                        RESTS_TABLE,
                        cv,
                        "nomenclature_id=? and stock_id=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("stock_id")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        RESTS_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }


        URI_ROUTES -> {
            Log.d(LOG_TAG, "URI_ROUTES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    db.update(ROUTES_TABLE, cv, "id=?", arrayOf<String?>(cv.getAsString("id")))
                    val rowID = db.insertWithOnConflict(
                        ROUTES_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }
                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            return numInserted
        }

        URI_ROUTES_LINES -> {
            Log.d(LOG_TAG, "URI_ROUTES_LINES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    db.update(
                        ROUTES_LINES_TABLE,
                        cv,
                        "route_id=? and lineno=?",
                        arrayOf<String?>(cv.getAsString("route_id"), cv.getAsString("lineno"))
                    )
                    val rowID = db.insertWithOnConflict(
                        ROUTES_LINES_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }
                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            return numInserted
        }

        URI_SALES_LOADED -> {
            Log.d(LOG_TAG, "URI_SALES_LOADED_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        SALES_LOADED_TABLE,
                        cv,
                        "nomenclature_id=? and refdoc=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("refdoc")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        SALES_LOADED_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_NOMENCLATURE -> {
            Log.d(LOG_TAG, "URI_NOMENCLATURE_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()


            /*
        SQLiteStatement stmt = db.compileStatement("SELECT max(_id) as _id FROM nomenclature WHERE id = ?");

        InsertHelper ih = new InsertHelper(db, NOMENCLATURE_TABLE);

        final int c__id = ih.getColumnIndex("_id");
        final int c_id = ih.getColumnIndex("id");
        final int c_isFolder = ih.getColumnIndex("isFolder");
        final int c_parent_id = ih.getColumnIndex("parent_id");
        final int c_code = ih.getColumnIndex("code");
        final int c_descr = ih.getColumnIndex("descr");
        final int c_descrFull = ih.getColumnIndex("descrFull");
        final int c_quant_1 = ih.getColumnIndex("quant_1");
        final int c_quant_2 = ih.getColumnIndex("quant_2");
        final int c_edizm_1_id = ih.getColumnIndex("edizm_1_id");
        final int c_edizm_2_id = ih.getColumnIndex("edizm_2_id");
        final int c_quant_k_1 = ih.getColumnIndex("quant_k_1");
        final int c_quant_k_2 = ih.getColumnIndex("quant_k_2");
        final int c_opt_price = ih.getColumnIndex("opt_price");
        final int c_m_opt_price = ih.getColumnIndex("m_opt_price");
        final int c_rozn_price = ih.getColumnIndex("rozn_price");
        final int c_incom_price = ih.getColumnIndex("incom_price");
        final int c_IsInPrice = ih.getColumnIndex("IsInPrice");
        final int c_flagWithoutDiscont = ih.getColumnIndex("flagWithoutDiscont");
        final int c_weight_k_1 = ih.getColumnIndex("weight_k_1");
        final int c_weight_k_2 = ih.getColumnIndex("weight_k_2");
        */
            // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        NOMENCLATURE_TABLE,
                        cv,
                        "id=?",
                        arrayOf<String?>(cv.getAsString("id"))
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        NOMENCLATURE_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }
                /*
            for (ContentValues cv : values) {

                stmt.bindString(1, cv.getAsString("id"));
                Long _idValue = stmt.simpleQueryForLong();

                if (_idValue>0)
                {
                    ih.prepareForReplace();
                    ih.bind(c__id, _idValue);
                } else
                {
                    ih.prepareForInsert();
                }

                ih.bind(c_id, cv.getAsString("id"));
                ih.bind(c_isFolder, cv.getAsString("isFolder"));
                ih.bind(c_parent_id, cv.getAsString("parent_id"));
                ih.bind(c_code, cv.getAsString("code"));
                ih.bind(c_descr, cv.getAsString("descr"));
                ih.bind(c_descrFull, cv.getAsString("descrFull"));
                ih.bind(c_quant_1, cv.getAsString("quant_1"));
                ih.bind(c_quant_2, cv.getAsString("quant_2"));
                ih.bind(c_edizm_1_id, cv.getAsString("edizm_1_id"));
                ih.bind(c_edizm_2_id, cv.getAsString("edizm_2_id"));
                ih.bind(c_quant_k_1, cv.getAsDouble("quant_k_1"));
                ih.bind(c_quant_k_2, cv.getAsDouble("quant_k_2"));
                ih.bind(c_opt_price, cv.getAsDouble("opt_price"));
                ih.bind(c_m_opt_price, cv.getAsDouble("m_opt_price"));
                ih.bind(c_rozn_price, cv.getAsDouble("rozn_price"));
                ih.bind(c_incom_price, cv.getAsString("incom_price"));
                ih.bind(c_IsInPrice, cv.getAsLong("IsInPrice"));
                ih.bind(c_flagWithoutDiscont, cv.getAsLong("flagWithoutDiscont"));
                ih.bind(c_weight_k_1, cv.getAsDouble("weight_k_1"));
                ih.bind(c_weight_k_2, cv.getAsDouble("weight_k_2"));

                ih.execute();
            }
            */
                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_NOMENCLATURE_HIERARCHY -> {
            Log.d(LOG_TAG, "URI_NOMENCLATURE_HIERARCHY_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()

            //long rowID = db.insertWithOnConflict(NOMENCLATURE_HIERARCHY_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //Uri resultUri = ContentUris.withAppendedId(NOMENCLATURE_HIERARCHY_CONTENT_URI, rowID);
            //getContext().getContentResolver().notifyChange(resultUri, null);
            //return resultUri;
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    val rowID = db.insertWithOnConflict(
                        NOMENCLATURE_HIERARCHY_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )
                }
                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_CLIENTS -> {
            Log.d(LOG_TAG, "URI_CLIENTS_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()


            // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(CLIENTS_TABLE, cv, "id=?", arrayOf<String?>(cv.getAsString("id")))


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        CLIENTS_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_DISTR_POINTS -> {
            Log.d(LOG_TAG, "URI_DISTR_POINTS_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()


            // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        DISTR_POINTS_TABLE,
                        cv,
                        "id=? and owner_id=?",
                        arrayOf<String?>(cv.getAsString("id"), cv.getAsString("owner_id"))
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        DISTR_POINTS_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_CLIENTS_PRICE -> {
            Log.d(LOG_TAG, "URI_CLIENTS_PRICE_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()


            // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        CLIENTS_PRICE_TABLE,
                        cv,
                        "nomenclature_id=? and client_id=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("client_id")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        CLIENTS_PRICE_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_CURATORS_PRICE -> {
            Log.d(LOG_TAG, "URI_CURATORS_PRICE_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()


            // http://web.utk.edu/~jplyon/sqlite/SQLite_optimization_FAQ.html#pragmas
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    //long newID = sqlDB.insertOrThrow(table, null, cv);
                    //Do an update if the constraints match
                    db.update(
                        CURATORS_PRICE_TABLE,
                        cv,
                        "nomenclature_id=? and curator_id=?",
                        arrayOf<String?>(
                            cv.getAsString("nomenclature_id"),
                            cv.getAsString("curator_id")
                        )
                    )


                    //This will return the id of the newly inserted row if no conflict
                    //It will also return the offending row without modifying it if in conflict
                    val rowID = db.insertWithOnConflict(
                        CURATORS_PRICE_TABLE,
                        null,
                        cv,
                        SQLiteDatabase.CONFLICT_IGNORE
                    )


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_ORDERS_LINES -> {
            Log.d(LOG_TAG, "URI_ORDERS_LINES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    val rowID = db.insert(ORDERS_LINES_TABLE, null, cv)


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_REFUNDS_LINES -> {
            Log.d(LOG_TAG, "URI_REFUNDS_LINES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    val rowID = db.insert(REFUNDS_LINES_TABLE, null, cv)


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        URI_ORDERS_PLACES -> {
            Log.d(LOG_TAG, "URI_ORDERS_PLACES_BULK_INSERT")
            val db = dbHelper!!.getWritableDatabase()
            db.execSQL("PRAGMA synchronous=NORMAL")
            db.beginTransaction()
            try {
                for (cv in values) {
                    val rowID = db.insert(ORDERS_PLACES_TABLE, null, cv)


                    //if (rowID <= 0) {
                    //    throw new SQLException("Failed to insert row into " + uri);
                    //}
                }

                db.setTransactionSuccessful()
                getContext()!!.getContentResolver().notifyChange(uri, null)
                numInserted = values.size
            } finally {
                db.endTransaction()
            }
            //db.close();
            return numInserted
        }

        else -> return super.bulkInsert(uri, values)
    }
}

override fun onCreate(): Boolean {
    dbHelper = DBHelper(getContext())

    return true
}

override fun query(
    uri: Uri, projection: Array<String?>?, selection: String?, selectionArgs: Array<String?>?,
    sortOrder: String?
): Cursor {
    var selection = selection
    val qb = SQLiteQueryBuilder()


    //String groupBy=null;
    var bAppendIdToSelection = false
    var appendIdPreffix = ""

    when (sUriMatcher.match(uri)) {
        URI_ORDERS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(ORDERS_TABLE)
            qb.setProjectionMap(ordersProjectionMap)
        }

        URI_ORDERS -> {
            qb.setTables(ORDERS_TABLE)
            qb.setProjectionMap(ordersProjectionMap)
        }

        URI_CASH_PAYMENTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(CASH_PAYMENTS_TABLE)
            qb.setProjectionMap(cashPaymentsProjectionMap)
        }

        URI_CASH_PAYMENTS -> {
            qb.setTables(CASH_PAYMENTS_TABLE)
            qb.setProjectionMap(cashPaymentsProjectionMap)
        }

        URI_REFUNDS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(REFUNDS_TABLE)
            qb.setProjectionMap(refundsProjectionMap)
        }

        URI_REFUNDS -> {
            qb.setTables(REFUNDS_TABLE)
            qb.setProjectionMap(refundsProjectionMap)
        }

        URI_JOURNAL_ID -> {
            bAppendIdToSelection = true
            appendIdPreffix = "journal."
            qb.setTables(JOURNAL_TABLE)
            qb.setProjectionMap(journalProjectionMap)
        }

        URI_JOURNAL -> {
            qb.setTables(JOURNAL_TABLE)
            qb.setProjectionMap(journalProjectionMap)
        }

        URI_CLIENTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(CLIENTS_TABLE)
            qb.setProjectionMap(clientsProjectionMap)
        }

        URI_CLIENTS -> {
            qb.setTables(CLIENTS_TABLE)
            qb.setProjectionMap(clientsProjectionMap)
        }

        URI_NOMENCLATURE_ID -> {
            bAppendIdToSelection = true
            qb.setTables(NOMENCLATURE_TABLE)
            qb.setProjectionMap(nomenclatureProjectionMap)
        }

        URI_NOMENCLATURE -> {
            qb.setTables(NOMENCLATURE_TABLE)
            qb.setProjectionMap(nomenclatureProjectionMap)
        }

        URI_NOMENCLATURE_HIERARCHY -> {
            qb.setTables(NOMENCLATURE_HIERARCHY_TABLE)
            qb.setProjectionMap(nomenclatureHierarchyProjectionMap)
        }

        URI_NOMENCLATURE_LIST -> {
            //if (Common.MEGA)
            //{
            //	qb.setTables(NOMENCLATURE_LIST_TABLE_MEGA);
            //    qb.setProjectionMap(nomenclatureListProjectionMapMega);
            //} else
            //{
            qb.setTables(NOMENCLATURE_LIST_TABLE)
            qb.setProjectionMap(nomenclatureListProjectionMap)
        }

        URI_NOMENCLATURE_SURFING -> {
            qb.setTables(NOMENCLATURE_SURFING_TABLE)
            qb.setProjectionMap(nomenclatureSurfingProjectionMap)
        }

        URI_RESTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(RESTS_TABLE)
            qb.setProjectionMap(restsProjectionMap)
        }

        URI_RESTS -> {
            qb.setTables(RESTS_TABLE)
            qb.setProjectionMap(restsProjectionMap)
        }

        URI_AGREEMENTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(AGREEMENTS_TABLE)
            qb.setProjectionMap(agreementsProjectionMap)
        }

        URI_AGREEMENTS -> {
            qb.setTables(AGREEMENTS_TABLE)
            qb.setProjectionMap(agreementsProjectionMap)
        }

        URI_AGREEMENTS30_ID -> {
            bAppendIdToSelection = true
            qb.setTables(AGREEMENTS30_TABLE)
            qb.setProjectionMap(agreements30ProjectionMap)
        }

        URI_AGREEMENTS30 -> {
            qb.setTables(AGREEMENTS30_TABLE)
            qb.setProjectionMap(agreements30ProjectionMap)
        }

        URI_VERSIONS -> {
            qb.setTables(VERSIONS_TABLE)
            qb.setProjectionMap(versionsProjectionMap)
        }

        URI_VERSIONS_SALES -> {
            qb.setTables(VERSIONS_SALES_TABLE)
            qb.setProjectionMap(versionsSalesProjectionMap)
        }

        URI_SEANCES -> {
            qb.setTables(SEANCES_TABLE)
            qb.setProjectionMap(seancesProjectionMap)
        }

        URI_PRICETYPES -> {
            qb.setTables(PRICETYPES_TABLE)
            qb.setProjectionMap(pricetypesProjectionMap)
        }

        URI_SALDO -> {
            qb.setTables(SALDO_TABLE)
            qb.setProjectionMap(saldoProjectionMap)
        }

        URI_SALDO_EXTENDED_ID -> {
            bAppendIdToSelection = true
            qb.setTables(SALDO_EXTENDED_TABLE)
            qb.setProjectionMap(saldoExtendedProjectionMap)
        }

        URI_SALDO_EXTENDED -> {
            qb.setTables(SALDO_EXTENDED_TABLE)
            qb.setProjectionMap(saldoExtendedProjectionMap)
        }

        URI_SALDO_EXTENDED_JOURNAL -> {
            qb.setTables(SALDO_EXTENDED_JOURNAL_TABLE)
            qb.setProjectionMap(saldoExtendedJournalProjectionMap)
        }

        URI_STOCKS -> {
            qb.setTables(STOCKS_TABLE)
            qb.setProjectionMap(stocksProjectionMap)
        }

        URI_PRICES -> {
            qb.setTables(PRICES_TABLE)
            qb.setProjectionMap(pricesProjectionMap)
        }

        URI_PRICES_AGREEMENTS30 -> {
            qb.setTables(PRICES_AGREEMENTS30_TABLE)
            qb.setProjectionMap(pricesAgreements30ProjectionMap)
        }

        URI_CLIENTS_PRICE -> {
            qb.setTables(CLIENTS_PRICE_TABLE)
            qb.setProjectionMap(clientsPriceProjectionMap)
        }

        URI_CURATORS_PRICE -> {
            qb.setTables(CURATORS_PRICE_TABLE)
            qb.setProjectionMap(curatorsPriceProjectionMap)
        }

        URI_SIMPLE_DISCOUNTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(SIMPLE_DISCOUNTS_TABLE)
            qb.setProjectionMap(simpleDiscountsProjectionMap)
        }

        URI_SIMPLE_DISCOUNTS -> {
            qb.setTables(SIMPLE_DISCOUNTS_TABLE)
            qb.setProjectionMap(simpleDiscountsProjectionMap)
        }

        URI_AGENTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(AGENTS_TABLE)
            qb.setProjectionMap(agentsProjectionMap)
        }

        URI_AGENTS -> {
            qb.setTables(AGENTS_TABLE)
            qb.setProjectionMap(agentsProjectionMap)
        }

        URI_CURATORS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(CURATORS_TABLE)
            qb.setProjectionMap(curatorsProjectionMap)
        }

        URI_CURATORS -> {
            qb.setTables(CURATORS_TABLE)
            qb.setProjectionMap(curatorsProjectionMap)
        }

        URI_CURATORS_LIST -> {
            qb.setTables(CURATORS_LIST_TABLE)
            qb.setProjectionMap(curatorsListProjectionMap)
        }

        URI_DISTR_POINTS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(DISTR_POINTS_TABLE)
            qb.setProjectionMap(distrPointsProjectionMap)
        }

        URI_DISTR_POINTS -> {
            qb.setTables(DISTR_POINTS_TABLE)
            qb.setProjectionMap(distrPointsProjectionMap)
        }

        URI_DISTR_POINTS_LIST -> {
            qb.setTables(DISTR_POINTS_LIST_TABLE)
            qb.setProjectionMap(distrPointsListProjectionMap)
        }

        URI_ORGANIZATIONS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(ORGANIZATIONS_TABLE)
            qb.setProjectionMap(organizationsProjectionMap)
        }

        URI_ORGANIZATIONS -> {
            qb.setTables(ORGANIZATIONS_TABLE)
            qb.setProjectionMap(organizationsProjectionMap)
        }

        URI_CLIENTS_WITH_SALDO -> {
            qb.setTables(CLIENTS_WITH_SALDO_TABLE)
            qb.setProjectionMap(clientsWithSaldoProjectionMap)
        }

        URI_ORDERS_LINES -> {
            qb.setTables(ORDERS_LINES_TABLE)
            qb.setProjectionMap(ordersLinesProjectionMap)
        }

        URI_REFUNDS_LINES -> {
            qb.setTables(REFUNDS_LINES_TABLE)
            qb.setProjectionMap(refundsLinesProjectionMap)
        }

        URI_ORDERS_JOURNAL -> {
            qb.setTables(ORDERS_JOURNAL_TABLE)
            qb.setProjectionMap(ordersJournalProjectionMap)
        }

        URI_CASH_PAYMENTS_JOURNAL -> {
            qb.setTables(CASH_PAYMENTS_JOURNAL_TABLE)
            qb.setProjectionMap(cashPaymentsJournalProjectionMap)
        }

        URI_AGREEMENTS_LIST -> {
            qb.setTables(AGREEMENTS_LIST_TABLE)
            qb.setProjectionMap(agreementsListProjectionMap)
        }

        URI_AGREEMENTS30_LIST -> {
            qb.setTables(AGREEMENTS30_LIST_TABLE)
            qb.setProjectionMap(agreements30ListProjectionMap)
        }

        URI_AGREEMENTS_WITH_SALDO_ONLY_LIST -> {
            qb.setTables(AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE)
            qb.setProjectionMap(agreementsListWithSaldoOnlyProjectionMap)
        }

        URI_AGREEMENTS30_WITH_SALDO_ONLY_LIST -> {
            qb.setTables(AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE)
            qb.setProjectionMap(agreements30ListWithSaldoOnlyProjectionMap)
        }

        URI_ORDERS_LINES_COMPLEMENTED -> {
            qb.setTables(ORDERS_LINES_COMPLEMENTED_TABLE)
            qb.setProjectionMap(ordersLinesComplementedProjectionMap)
        }

        URI_REFUNDS_LINES_COMPLEMENTED -> {
            qb.setTables(REFUNDS_LINES_COMPLEMENTED_TABLE)
            qb.setProjectionMap(refundsLinesComplementedProjectionMap)
        }

        URI_VICARIOUS_POWER_ID -> {
            bAppendIdToSelection = true
            qb.setTables(VICARIOUS_POWER_TABLE)
            qb.setProjectionMap(vicariousPowerProjectionMap)
        }

        URI_VICARIOUS_POWER -> {
            qb.setTables(VICARIOUS_POWER_TABLE)
            qb.setProjectionMap(vicariousPowerProjectionMap)
        }

        URI_MESSAGES -> {
            qb.setTables(MESSAGES_TABLE)
            qb.setProjectionMap(messagesProjectionMap)
        }

        URI_MESSAGES_LIST -> {
            qb.setTables(MESSAGES_LIST_TABLE)
            qb.setProjectionMap(messagesListProjectionMap)
        }

        URI_SETTINGS -> {
            qb.setTables(SETTINGS_TABLE)
            qb.setProjectionMap(settingsProjectionMap)
        }

        URI_SALES_LOADED -> {
            qb.setTables(SALES_LOADED_TABLE)
            qb.setProjectionMap(salesLoadedProjectionMap)
        }

        URI_SALES_L2 -> {
            qb.setTables(SALES_L2_TABLE)
            qb.setProjectionMap(salesL2ProjectionMap)
        }

        URI_PLACES -> {
            qb.setTables(PLACES_TABLE)
            qb.setProjectionMap(placesProjectionMap)
        }

        URI_OCCUPIED_PLACES -> {
            qb.setTables(OCCUPIED_PLACES_TABLE)
            qb.setProjectionMap(occupiedPlacesProjectionMap)
        }

        URI_ORDERS_PLACES -> {
            qb.setTables(ORDERS_PLACES_TABLE)
            qb.setProjectionMap(ordersPlacesProjectionMap)
        }

        URI_ORDERS_PLACES_LIST -> {
            qb.setTables(ORDERS_PLACES_LIST_TABLE)
            qb.setProjectionMap(ordersPlacesListProjectionMap)
        }

        URI_GPS_COORD -> {
            qb.setTables(GPS_COORD_TABLE)
            qb.setProjectionMap(gpsCoordProjectionMap)
        }

        URI_DISTRIBS_CONTRACTS -> {
            qb.setTables(DISTRIBS_CONTRACTS_TABLE)
            qb.setProjectionMap(distribsContractsProjectionMap)
        }

        URI_DISTRIBS_CONTRACTS_LIST -> {
            qb.setTables(DISTRIBS_CONTRACTS_LIST_TABLE)
            qb.setProjectionMap(distribsContractsListProjectionMap)
        }

        URI_DISTRIBS_LINES -> {
            qb.setTables(DISTRIBS_LINES_TABLE)
            qb.setProjectionMap(distribsLinesProjectionMap)
        }

        URI_DISTRIBS_LINES_COMPLEMENTED -> {
            qb.setTables(DISTRIBS_LINES_COMPLEMENTED_TABLE)
            qb.setProjectionMap(distribsLinesComplementedProjectionMap)
        }

        URI_DISTRIBS_ID -> {
            bAppendIdToSelection = true
            qb.setTables(DISTRIBS_TABLE)
            qb.setProjectionMap(distribsProjectionMap)
        }

        URI_DISTRIBS -> {
            qb.setTables(DISTRIBS_TABLE)
            qb.setProjectionMap(distribsProjectionMap)
        }

        URI_EQUIPMENT -> {
            qb.setTables(EQUIPMENT_TABLE)
            qb.setProjectionMap(equipmentProjectionMap)
        }

        URI_EQUIPMENT_RESTS -> {
            qb.setTables(EQUIPMENT_RESTS_TABLE)
            qb.setProjectionMap(equipmentRestsProjectionMap)
        }

        URI_EQUIPMENT_RESTS_LIST -> {
            qb.setTables(EQUIPMENT_RESTS_LIST_TABLE)
            qb.setProjectionMap(equipmentRestsListProjectionMap)
        }

        URI_MTRADE_LOG -> {
            qb.setTables(MTRADE_LOG_TABLE)
            qb.setProjectionMap(mtradeLogProjectionMap)
        }

        URI_PERMISSIONS_REQUESTS -> {
            qb.setTables(PERMISSIONS_REQUESTS_TABLE)
            qb.setProjectionMap(permissionsRequestsProjectionMap)
        }

        URI_ROUTES -> {
            qb.setTables(ROUTES_TABLE)
            qb.setProjectionMap(routesProjectionMap)
        }

        URI_ROUTES_LINES -> {
            qb.setTables(ROUTES_LINES_TABLE)
            qb.setProjectionMap(routesLinesProjectionMap)
        }

        URI_ROUTES_DATES_ID -> {
            bAppendIdToSelection = true
            qb.setTables(ROUTES_DATES_TABLE)
            qb.setProjectionMap(routesDatesProjectionMap)
        }

        URI_ROUTES_DATES -> {
            qb.setTables(ROUTES_DATES_TABLE)
            qb.setProjectionMap(routesDatesProjectionMap)
        }

        URI_ROUTES_DATES_LIST_ID -> {
            bAppendIdToSelection = true
            appendIdPreffix = "routes_dates."
            qb.setTables(ROUTES_DATES_LIST_TABLE)
            qb.setProjectionMap(routesDatesListProjectionMap)
        }

        URI_ROUTES_DATES_LIST -> {
            qb.setTables(ROUTES_DATES_LIST_TABLE)
            qb.setProjectionMap(routesDatesListProjectionMap)
        }

        URI_REAL_ROUTES_DATES_ID -> {
            bAppendIdToSelection = true
            qb.setTables(REAL_ROUTES_DATES_TABLE)
            qb.setProjectionMap(realRoutesDatesProjectionMap)
        }

        URI_REAL_ROUTES_DATES -> {
            qb.setTables(REAL_ROUTES_DATES_TABLE)
            qb.setProjectionMap(realRoutesDatesProjectionMap)
        }

        URI_REAL_ROUTES_LINES -> {
            qb.setTables(REAL_ROUTES_LINES_TABLE)
            qb.setProjectionMap(realRoutesLinesProjectionMap)
        }

        else -> throw IllegalArgumentException("Unknown URI (for select) " + uri)
    }

    if (bAppendIdToSelection) {
        if (selection == null || selection.isEmpty()) selection =
            appendIdPreffix + "_id = " + uri.getLastPathSegment()
        else selection =
            selection + " and " + appendIdPreffix + "_id = " + uri.getLastPathSegment()
    }

    val db = dbHelper!!.getReadableDatabase()

    //SQLiteDatabase db = dbHelper.getWritableDatabase();

    //ContentValues cv = new ContentValues();
    Log.d(LOG_TAG, " --- onQuery --- ")


    //cv.clear();
    //cv.put("numdoc", "0001");
    //cv.put("datedoc", "20130101");
    //db.insert("orders", null, cv);

    //String s=qb.buildQuery(projection, selection, selectionArgs, groupBy, null, sortOrder, null);

    /*
    Cursor c1;
    try
    {
        c1 = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    } catch (SQLException e)
    {
        Log.e(LOG_TAG, e.toString());
    }
    */
    val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
    // просим ContentResolver уведомлять этот курсор
    // об изменениях данных в CONTACT_CONTENT_URI
    c.setNotificationUri(getContext()!!.getContentResolver(), uri)
    //db.close();
    return c
}

override fun update(
    uri: Uri,
    cv: ContentValues?,
    where: String?,
    selectionArgs: Array<String?>?
): Int {
    var where = where
    var numUpdated = 0
    when (sUriMatcher.match(uri)) {
        URI_ORDERS_ID -> {
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update orders set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update orders set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(ORDERS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            ORDERS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            ORDERS_JOURNAL_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )

                    var journal_id: Long = 0
                    val cursor = db.query(
                        JOURNAL_TABLE,
                        arrayOf<String>("journal._id"),
                        "order_id=?",
                        arrayOf<String>(
                            _id
                        ),
                        null,
                        null,
                        null
                    )
                    if (cursor.moveToNext()) {
                        journal_id = cursor.getLong(0)
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                    cursor.close()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_ORDERS -> {
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update orders set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update orders set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(ORDERS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(ORDERS_CONTENT_URI, null)
                    getContext()!!.getContentResolver()
                        .notifyChange(ORDERS_JOURNAL_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_ORDERS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(ORDERS_LINES_TABLE, cv, where, selectionArgs)
        }

        URI_ORDERS_SILENT -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(ORDERS_TABLE, cv, where, selectionArgs)
        }

        URI_REFUNDS_ID -> {
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update refunds set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update refunds set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(REFUNDS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            REFUNDS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )

                    var journal_id: Long = 0
                    val cursor = db.query(
                        JOURNAL_TABLE,
                        arrayOf<String>("journal._id"),
                        "refund_id=?",
                        arrayOf<String>(
                            _id
                        ),
                        null,
                        null,
                        null
                    )
                    if (cursor.moveToNext()) {
                        journal_id = cursor.getLong(0)
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                    cursor.close()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_REFUNDS -> {
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update refunds set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update refunds set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(REFUNDS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(REFUNDS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_REFUNDS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(REFUNDS_LINES_TABLE, cv, where, selectionArgs)
        }

        URI_DISTRIBS_ID -> {
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update distribs set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update distribs set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(DISTRIBS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            DISTRIBS_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )

                    var journal_id: Long = 0
                    val cursor = db.query(
                        JOURNAL_TABLE,
                        arrayOf<String>("journal._id"),
                        "distribs_id=?",
                        arrayOf<String>(
                            _id
                        ),
                        null,
                        null,
                        null
                    )
                    if (cursor.moveToNext()) {
                        journal_id = cursor.getLong(0)
                        getContext()!!.getContentResolver().notifyChange(
                            ContentUris.withAppendedId(
                                JOURNAL_CONTENT_URI, journal_id
                            ), null
                        )
                    }
                    cursor.close()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_DISTRIBS -> {
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update distribs set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update distribs set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(DISTRIBS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(DISTRIBS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(JOURNAL_CONTENT_URI, null)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_DISTRIBS_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(DISTRIBS_LINES_TABLE, cv, where, selectionArgs)
        }

        URI_NOMENCLATURE -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(NOMENCLATURE_TABLE, cv, where, selectionArgs)
        }

        URI_CLIENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(CLIENTS_TABLE, cv, where, selectionArgs)
        }

        URI_PRICETYPES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(PRICETYPES_TABLE, cv, where, selectionArgs)
        }

        URI_PRICES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(PRICES_TABLE, cv, where, selectionArgs)
        }

        URI_PRICES_AGREEMENTS30 -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(PRICES_AGREEMENTS30_TABLE, cv, where, selectionArgs)
        }

        URI_SALDO -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(SALDO_TABLE, cv, where, selectionArgs)
        }

        URI_SALDO_EXTENDED -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(SALDO_EXTENDED_TABLE, cv, where, selectionArgs)
        }

        URI_RESTS -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(RESTS_TABLE, cv, where, selectionArgs)
        }

        URI_SIMPLE_DISCOUNTS -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(SIMPLE_DISCOUNTS_TABLE, cv, where, selectionArgs)
        }

        URI_SETTINGS -> {
            val db = dbHelper!!.getWritableDatabase()
            var cnt = db.update(SETTINGS_TABLE, cv, where, selectionArgs)
            if (cnt == 0) {
                db.insert(SETTINGS_TABLE, null, cv)
                cnt = 1
            }
            return cnt
        }

        URI_PLACES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(PLACES_TABLE, cv, where, selectionArgs)
        }

        URI_OCCUPIED_PLACES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(OCCUPIED_PLACES_TABLE, cv, where, selectionArgs)
        }

        URI_MESSAGES_ID -> {
            val _id = uri.getLastPathSegment()
            if (where == null || where.isEmpty()) {
                where = "_id=" + _id
            } else {
                where += " and _id=" + _id
            }
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (cv!!.containsKey("isMark") && cv.getAsInteger("isMark") == 1) {
                    // Версию увеличивать не нужно, это мы помечаем на удаление
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update messages set isMarkCnt=isMarkCnt+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        db.execSQL("update messages set isMarkCnt=isMarkCnt+1")
                    }
                } else {
                    if (!cv.containsKey("ver")) {
                        // если версия не записывается, увеличим ее на единицу
                        // (скопировано из заказа, возможно не требуется)
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update messages set ver=ver+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update messages set ver=ver+1")
                        }
                    }
                }
                numUpdated = db.update(MESSAGES_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            MESSAGES_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                    getContext()!!.getContentResolver().notifyChange(
                        ContentUris.withAppendedId(
                            MESSAGES_LIST_CONTENT_URI, _id!!.toInt().toLong()
                        ), null
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_MESSAGES -> {
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (cv!!.containsKey("isMark") && cv.getAsInteger("isMark") == 1) {
                    // Версию увеличивать не нужно, это мы помечаем на удаление
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update messages set isMarkCnt=isMarkCnt+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        db.execSQL("update messages set isMarkCnt=isMarkCnt+1")
                    }
                } else {
                    if (!cv.containsKey("ver")) {
                        // если версия не записывается, увеличим ее на единицу
                        if (where != null && !where.isEmpty()) {
                            db.execSQL(
                                "update messages set ver=ver+1 where " + where,
                                if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                            )
                        } else {
                            // на самом деле такого не будет
                            db.execSQL("update messages set ver=ver+1")
                        }
                    }
                }
                numUpdated = db.update(MESSAGES_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver().notifyChange(MESSAGES_CONTENT_URI, null)
                    getContext()!!.getContentResolver()
                        .notifyChange(MESSAGES_LIST_CONTENT_URI, null)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_CASH_PAYMENTS -> {
            val db = dbHelper!!.getWritableDatabase()
            db.beginTransaction()
            try {
                if (!cv!!.containsKey("versionPDA")) {
                    // если версия не записывается, увеличим ее на единицу
                    if (where != null && !where.isEmpty()) {
                        db.execSQL(
                            "update cash_payments set versionPDA=versionPDA+1 where " + where,
                            if (selectionArgs == null) arrayOf<String?>() else selectionArgs
                        )
                    } else {
                        // на самом деле такого не будет
                        db.execSQL("update cash_payments set versionPDA=versionPDA+1")
                    }
                }
                numUpdated = db.update(CASH_PAYMENTS_TABLE, cv, where, selectionArgs)
                if (numUpdated > 0) {
                    getContext()!!.getContentResolver()
                        .notifyChange(CASH_PAYMENTS_CONTENT_URI, null)
                    getContext()!!.getContentResolver().notifyChange(
                        CASH_PAYMENTS_JOURNAL_CONTENT_URI, null
                    )
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return numUpdated
        }

        URI_GPS_COORD -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(GPS_COORD_TABLE, cv, where, selectionArgs)
        }

        URI_DISTRIBS_CONTRACTS -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(DISTRIBS_CONTRACTS_TABLE, cv, where, selectionArgs)
        }

        URI_EQUIPMENT -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(EQUIPMENT_TABLE, cv, where, selectionArgs)
        }

        URI_EQUIPMENT_RESTS -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(EQUIPMENT_RESTS_TABLE, cv, where, selectionArgs)
        }

        URI_MTRADE_LOG -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(MTRADE_LOG_TABLE, cv, where, selectionArgs)
        }

        URI_ROUTES_DATES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(ROUTES_DATES_TABLE, cv, where, selectionArgs)
        }

        URI_ROUTES_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(ROUTES_LINES_TABLE, cv, where, selectionArgs)
        }

        URI_REAL_ROUTES_DATES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(REAL_ROUTES_DATES_TABLE, cv, where, selectionArgs)
        }

        URI_REAL_ROUTES_LINES -> {
            val db = dbHelper!!.getWritableDatabase()
            return db.update(REAL_ROUTES_LINES_TABLE, cv, where, selectionArgs)
        }

        else -> throw IllegalArgumentException("Unknown URI (for update)" + uri)
    }

    companion object {
        private const val LOG_TAG = "mtradeLogs"


        // При забивании количества товара, превышающего остаток, должно выводится предупреждающее
        // сообщение «не хватает … шт. Продолжить?». (Строки по недостающим товарам выделять красным)
        // У нас уже в заявке есть красные строки – в случае изменения первоначального количества в заявке показываются оба количества и строка красная. Как их отличить?
        // ну пусть в этом случае будет зеленая ) , если можно.
        // Думаю, красным должно выделяться то, чего не хватает в данный момент (так агентам будет понятнее). Если после обмена товара стало хватать, то строка становится бесцветная. И наоборот.
        // Таким образом, внутри заявки строки могут быть трёх цветов: бесцветная (всё нормально), красная (не хватает товара), зеленая (количество товара изменено).
        val JOURNAL_ORDER_COLOR_FOR_TRIGGER: String =
            "case when new.dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when new.state=" + E_ORDER_STATE.E_ORDER_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                    "when new.closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                    "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_ORDER_STATE.E_ORDER_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end"

        val JOURNAL_REFUND_COLOR_FOR_TRIGGER: String =
            "case when new.dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when new.state=" + E_REFUND_STATE.E_REFUND_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                    "when new.closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                    "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_REFUND_STATE.E_REFUND_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end"

        val JOURNAL_DISTRIBS_COLOR_FOR_TRIGGER: String =
        // сначала состояния, при которых данные отправляются
                /**/////////////////////////////////////// */ // запрос отмены - серый
            "case when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                    "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                    "when new.versionPDA<>new.versionPDA_ack and (new.state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end"

        // в принципе, этот журнал используется только если не используется общий журнал
        // (скорее всего он уже не используется совсем)
        val ORDERS_COLOR_COLUMN: String =
            "case when dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when state=" + E_ORDER_STATE.E_ORDER_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                    "when closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                    "when versionPDA<>versionPDA_ack and (state=" + E_ORDER_STATE.E_ORDER_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end as color"

        val CASH_PAYMENTS_COLOR_COLUMN: String =
        // сначала состояния, при которых данные отправляются
                /**/////////////////////////////////////// */ // запрос отмены - серый
            "case when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                    "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                    "when versionPDA<>versionPDA_ack and (state=" + E_PAYMENT_STATE.E_PAYMENT_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end as color"

        val REFUNDS_COLOR_COLUMN: String =
            "case when dont_need_send=1 then 1 " +  // сначала состояния, при которых данные отправляются
                    /**/////////////////////////////////////// */ // запрос отмены - серый
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_QUERY_CANCEL.value() + " then 2 " +  // согласование - оранжевый
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_WAITING_AGREEMENT.value() + " then 6 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_CANCELED.value() + " then 7 " +  // восстановлен, сбой - красный
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_BACKUP_NOT_SAVED.value() + " then 3 " +  // неизвестное - красный
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when state=" + E_REFUND_STATE.E_REFUND_STATE_SENT.value() + " then 5 " +  // выполнена, но не все было в наличии (которая промежуточно устанавливалась в состояние "согласована", количество не совпадает с требуемым)
                    "when closed_not_full=1 then 4 " +  // создана и будет отправляться - зеленая
                    "when versionPDA<>versionPDA_ack and (state=" + E_REFUND_STATE.E_REFUND_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end as color"

        val DISTRIBS_COLOR_COLUMN: String =  // сначала состояния, при которых данные отправляются
                /**/////////////////////////////////////// */ // запрос отмены - серый
            "case when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_QUERY_CANCEL.value() + " then 2 " +  // а потом все остальные
                    /**////////////////////////////////////// */ // отменен - красный
                    "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CANCELED.value() + " then 7 " +  // неизвестное - красный
                    "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_UNKNOWN.value() + " then 3 " +  // отправлен - голубой
                    "when state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_SENT.value() + " then 5 " +  // создана и будет отправляться - зеленая
                    "when versionPDA<>versionPDA_ack and (state=" + E_DISTRIBS_STATE.E_DISTRIBS_STATE_CREATED.value() + ") then 10 " +
                    "else 0 end as color"


        const val CLIENT_DESCR_COLUMN: String =
            "CASE WHEN orders.create_client=1 THEN orders.create_client_descr ELSE IFNULL(clients.descr, \"{\"||orders.client_id||\"}\") END as client_descr"
        const val CASH_PAYMENTS_CLIENT_DESCR_COLUMN: String =
            "IFNULL(clients.descr, \"{\"||cash_payments.client_id||\"}\") as client_descr"
        const val SALDO_CLIENT_DESCR_COLUMN: String =
            "IFNULL(clients.descr, \"{\"||saldo_extended.client_id||\"}\") as client_descr"
        const val SALDO_AGREEMENT_DESCR_COLUMN: String =
            "IFNULL(agreements.descr, saldo_extended.agreement_descr) as agreement_descr"
        const val AGREEMENT_DESCR_COLUMN: String = "agreements.descr as agreement_descr"
        const val AGREEMENT30_DESCR_COLUMN: String = "agreements30.descr as agreement_descr"
        const val ORGANIZATION_DESCR_COLUMN: String = "organizations.descr as organization_descr"
        const val PRICE_TYPE_DESCR_COLUMN: String = "pricetypes.descr as pricetype_descr"
        const val NOMENCLATURE_DESCR_COLUMN: String =
            "IFNULL(nomenclature.descr, \"{\"||nomenclature_id||\"}\") as nomencl_descr"
        const val NOMENCLATURE_WEIGHT_COLUMN: String =
            "nomenclature.weight_k_1 as nomencl_weight_k_1"
        const val NOMENCLATURE_ID_COLUMN: String = "nomenclature.id as nomenclature_id"
        const val NOMENCLATURE_ID_SURFING_COLUMN: String =
            "ifnull(nomenclature.id, h.id) as nomenclature_id"
        const val NOMENCLATURE_DESCR_SURFING_COLUMN: String =
            "ifnull(nomenclature.descr, h.groupDescr) as descr"
        const val NOMENCLATURE_GROUP_COLUMN: String = "h.groupDescr as h_groupDescr"

        const val CONTRACT_DESCR_COLUMN: String =
            "IFNULL(distribsContracts.descr, \"{\"||contract_id||\"}\") as contract_descr"


        const val NOMENCLATURE_REST_STOCK_COLUMN: String =
            "restsS.quantity-restsS.quantity_reserve as nom_quantity"
        const val NOMENCLATURE_SALES_COLUMN: String = "restsS.saledNow as nom_quantity_saled_now"
        const val DUMMY_COLUMN: String = "0 as zero"

        // БД
        //private static final String DIR = "/sdcard";
        const val DB_NAME: String = "mdata.db"
        const val DB_VERSION: Int = 75

        // Таблица
        private const val ORDERS_TABLE = "orders"
        private const val CASH_PAYMENTS_TABLE = "cash_payments"
        private const val REFUNDS_TABLE = "refunds"
        private const val CLIENTS_TABLE = "clients"
        private const val NOMENCLATURE_TABLE = "nomenclature"

        private val NOMENCLATURE_LIST_TABLE =
            "nomenclature left join rests_sales_stuff as restsS on restsS.nomenclature_id=nomenclature.id " +
                    "left join salesV as sales_client on sales_client.nomenclature_id=nomenclature.id or sales_client.nomenclature_id=nomenclature.parent_id " +
                    "left join pricesV as prices on prices.nomenclature_id=nomenclature.id " +  // здесь хранятся данные как товаров, так и групп, но нам важно для групп только данные по группам, а по товарам и не нужно - в выборку они не включены
                    "left join salesV_7 as sales7 on sales7.nomenclature_id=nomenclature.id " +
                    "left join nomenclature_hierarchy h on h.id=nomenclature.id " +
                    "left join nomenclature_hierarchy h_parent on h_parent.id=nomenclature.parent_id " +
                    "left join discounts_stuff d_s on d_s.nomenclature_id=nomenclature.id " +
                    "left join discounts_stuff d_s_p on d_s_p.nomenclature_id=nomenclature.parent_id"

        // отличие от NOMENCLATURE_LIST_TABLE в том, что корневой элемент (он только в иерархии) должен присутствоать
        private val NOMENCLATURE_SURFING_TABLE: String = NOMENCLATURE_LIST_TABLE

        /*
            "nomenclature_hierarchy h "+
            "left join nomenclature on nomenclature.id=h.id " +
                    "left join rests_sales_stuff as restsS on restsS.nomenclature_id=nomenclature.id "+
                    "left join salesV as sales_client on sales_client.nomenclature_id=nomenclature.id or sales_client.nomenclature_id=nomenclature.parent_id "+
                    "left join pricesV as prices on prices.nomenclature_id=nomenclature.id "+
                    // здесь хранятся данные как товаров, так и групп, но нам важно для групп только данные по группам, а по товарам и не нужно - в выборку они не включены
                    "left join salesV_7 as sales7 on sales7.nomenclature_id=nomenclature.id "+
                    "left join nomenclature_hierarchy h_parent on h_parent.id=nomenclature.parent_id " +
                    "left join discounts_stuff d_s on d_s.nomenclature_id=nomenclature.id " +
                    "left join discounts_stuff d_s_p on d_s_p.nomenclature_id=nomenclature.parent_id";
            */
        private const val RESTS_TABLE = "rests"
        private const val SALDO_TABLE = "saldo"
        private const val SALDO_EXTENDED_TABLE = "saldo_extended"
        private const val SALDO_EXTENDED_JOURNAL_TABLE =
            "saldo_extended left join clients on clients.id=saldo_extended.client_id left join agreements on agreements.id=saldo_extended.agreement_id"
        private const val AGREEMENTS_TABLE = "agreements"
        private const val AGREEMENTS30_TABLE = "agreements30"
        private const val VERSIONS_TABLE = "versions"
        private const val VERSIONS_SALES_TABLE = "sales_versions"
        private const val PRICETYPES_TABLE = "pricetypes"
        private const val STOCKS_TABLE = "stocks"
        private const val PRICES_TABLE = "prices"
        private const val PRICES_AGREEMENTS30_TABLE = "prices_agreements30"
        private const val AGENTS_TABLE = "agents"
        private const val CURATORS_TABLE = "curators"
        private const val PERMISSIONS_REQUESTS_TABLE = "permissions_requests"
        private const val ROUTES_TABLE = "routes"
        private const val ROUTES_LINES_TABLE = "routes_lines"
        private const val ROUTES_DATES_TABLE = "routes_dates"
        private const val ROUTES_DATES_LIST_TABLE =
            "routes_dates left join routes on routes.id=routes_dates.route_id"

        private const val REAL_ROUTES_DATES_TABLE = "real_routes_dates"
        private const val REAL_ROUTES_LINES_TABLE = "real_routes_lines"

        private val CURATORS_LIST_TABLE = "(select" +
                " _id*2 as _id,id,isFolder,parent_id,null as client_id,code,descr from curators " +
                " union all" +
                " select" +
                " max(saldo_extended._id)*2+1,manager_id,2,null,client_id,manager_descr,manager_descr from saldo_extended" +
                " left join curators on curators.id=manager_id" +
                " where curators.id is null" +
                " group by manager_id,client_id,manager_descr,manager_descr" +
                ") cr"

        private const val DISTR_POINTS_TABLE = "distr_points"
        private const val DISTR_POINTS_LIST_TABLE =
            "distr_points left join pricetypes on pricetypes.id=distr_points.price_type_id"
        private const val ORGANIZATIONS_TABLE = "organizations"
        private const val CLIENTS_WITH_SALDO_TABLE =
            "clients left join saldo on saldo.client_id=clients.id"
        private const val ORDERS_LINES_TABLE = "ordersLines"
        private const val REFUNDS_LINES_TABLE = "refundsLines"
        private const val ORDERS_JOURNAL_TABLE =
            "orders left join clients on clients.id=orders.client_id"
        private const val JOURNAL_TABLE =
            "journal left join clients on clients.id=journal.client_id"

        private const val CASH_PAYMENTS_JOURNAL_TABLE =
            "cash_payments left join clients on clients.id=cash_payments.client_id"
        private const val AGREEMENTS_LIST_TABLE =
            "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id"
        private const val AGREEMENTS30_LIST_TABLE =
            "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id"

        private val AGREEMENTS_LIST_WITH_ONLY_SALDO_TABLE =
            "agreements left join organizations on organizations.id=agreements.organization_id left join pricetypes on pricetypes.id=agreements.price_type_id " +
                    "left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements.id"
        private val AGREEMENTS30_LIST_WITH_ONLY_SALDO_TABLE =
            "agreements30 left join organizations on organizations.id=agreements30.organization_id left join pricetypes on pricetypes.id=agreements30.price_type_id " +
                    "left join (select agreement_id, sum(saldo) saldo, sum(saldo_past) saldo_past from saldo_extended group by agreement_id) saldo0 on saldo0.agreement_id=agreements30.id"

        private const val ORDERS_LINES_COMPLEMENTED_TABLE =
            "ordersLines left join nomenclature on nomenclature.id=ordersLines.nomenclature_id"
        private const val REFUNDS_LINES_COMPLEMENTED_TABLE =
            "refundsLines left join nomenclature on nomenclature.id=refundsLines.nomenclature_id"
        private const val SEANCES_TABLE = "seances"
        private const val VICARIOUS_POWER_TABLE = "vicarious_power"
        private const val GPS_COORD_TABLE = "gps_coord"
        private const val MESSAGES_TABLE = "messages"
        private val MESSAGES_LIST_TABLE =
            "messages left join clients on clients.id=messages.client_id and type_idx in (" + E_MESSAGE_TYPES.E_MESSAGE_TYPE_DEBT.value() + "," + E_MESSAGE_TYPES.E_MESSAGE_TYPE_SALES.value() + ") left join agents on agents.id=messages.sender_id and acknowledged&4==0 or agents.id=messages.receiver_id and acknowledged&4!=0"

        //private static final String SALES_TABLE = "sales";
        private const val CLIENTS_PRICE_TABLE = "clients_price"
        private const val CURATORS_PRICE_TABLE = "curators_price"
        private const val SETTINGS_TABLE = "settings"
        private const val SALES_LOADED_TABLE = "salesloaded"

        private const val SALES_LOADED_WITH_COMMON_GROUPS_TABLE =
            "(select max(s._id) as _id, datedoc, numdoc, client_id, case when p.group_of_analogs=1 then p.id else s.nomenclature_id end as nomenclature_id, sum(quantity) as quantity, price from salesloaded s left join nomenclature n on n.id=s.nomenclature_id left join nomenclature p on p.id=n.parent_id group by case when p.group_of_analogs=1 then p.id else s.nomenclature_id end, datedoc, numdoc, client_id, price)"

        private const val NOMENCLATURE_HIERARCHY_TABLE = "nomenclature_hierarchy"
        private const val SALES_L_TABLE = "salesL"
        private const val SALES_L2_TABLE = "salesL2"
        private const val SIMPLE_DISCOUNTS_TABLE = "simple_discounts"

        private const val PLACES_TABLE = "places"
        private const val OCCUPIED_PLACES_TABLE = "occupied_places"
        private const val ORDERS_PLACES_TABLE = "ordersPlaces"
        private const val ORDERS_PLACES_LIST_TABLE =
            "orders join ordersPlaces on ordersPlaces.order_id=orders._id"

        private const val DISTRIBS_CONTRACTS_TABLE = "distribsContracts"
        private const val DISTRIBS_CONTRACTS_LIST_TABLE = "distribsContractsList" // TODO
        private const val DISTRIBS_LINES_TABLE = "distribsLines"
        private const val DISTRIBS_LINES_COMPLEMENTED_TABLE =
            "distribsLines left join distribsContracts on distribsContracts.id=distribsLines.contract_id"
        private const val DISTRIBS_TABLE = "distribs"

        private const val EQUIPMENT_TABLE = "equipment"
        private const val EQUIPMENT_RESTS_TABLE = "equipment_rests"
        private const val EQUIPMENT_RESTS_LIST_TABLE =
            "equipment_rests left join equipment on equipment.id=equipment_rests.nomenclature_id"

        private const val MTRADE_LOG_TABLE = "mtradelog"
    }
}