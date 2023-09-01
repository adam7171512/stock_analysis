# Stock analyser

This project was inspired by curiosity to inspect the price action of stocks near the dividend periods.
It is a very early stage prototype. It's mostly built in Java, however Python was used for data scraping (gpw prices from gpw website, dividend info from stockwatch.pl) and graphing.

It allows to backtest performance of particular tickers with some logic like buying/selling the stock X days before/after ex-div date.
It also allows to perform portfolio analysis with asset rotation, and some simple logic to include interests generated on free cash.

At first, I've fetched the data into TimescaleDb running locally, however later I switched to File System for much faster backtesting, thus two repository types are implemented.

Some interesting results obtained using the tool:
Price action near ex-div date for particular set of tickers:

![image](https://github.com/adam7171512/gpw_div_backtest/assets/117537530/44c12a47-51d7-4063-a9d6-fda61b2c76cd)


Some example portfolio strategies comparison :

![image](https://github.com/adam7171512/gpw_div_backtest/assets/117537530/4562f403-25f8-4024-911c-a98f6fe1c57f)
