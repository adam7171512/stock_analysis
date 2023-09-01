# Stock analyser

This project was inspired by curiosity to inspect the price action of stocks near the dividend periods and look for some inefficiencies in the market.
It is a **very** early stage prototype. It's mostly built in Java, however Python was used for data scraping (gpw prices from gpw website, dividend info from stockwatch.pl) and graphing using simple Flask endpoints.

It's not ready to run "as is" , as there are some ETL tools I've used to create data compatible with my repository interfaces that I have not yet added to the project.

It allows to backtest performance of particular tickers with some logic like buying/selling the stock X days before/after ex-div date.
It also allows to perform portfolio analysis with asset rotation, and some simple logic to include interests generated on free cash.

At first, I've fetched the data into TimescaleDb running locally, however later I switched to File System for much faster backtesting, thus two repository types are implemented.

The Main object representing state of Portfolio in Portfolio Analysis is PortfolioSnapshot. It freezes the Portfolio in time and defines it's composition and value.

PortfolioStrategyAnalysis class is the key analysing component for Portfolio Analysis. It takes tickers to analyse, dividend repository, ohlc repository and performs time series analysis creating snapshots at each step, aswell as decides if/when to rebalance the portfolio and when to dispose/buy assets based on ex-div date.

Some interesting results obtained using the tool:

Some example portfolio strategies comparison (One of strategies keeps static number of holdings, other rotates them) :

![image](https://github.com/adam7171512/gpw_div_backtest/assets/117537530/2fd4b617-5f1f-4792-bdaf-37e28e17e9b3)
Price action near ex-div date for particular filtered set of tickers:

![image](https://github.com/adam7171512/gpw_div_backtest/assets/117537530/1fc63b3f-2bb7-44ae-9fdf-94390f4fdd45)


