import pandas as pd
import requests
from bs4 import BeautifulSoup
from pydantic import BaseModel
import datetime

class DividendInfo(BaseModel):
    ticker: str
    amount: float
    date_ex_div: datetime.date
    date_payment: datetime.date | None = None
    date_decision: datetime.date | None = None
    yield_percentage: float | None = None

    # to avoid collecting duplicates
    def __hash__(self):
        return hash(self.ticker) + hash(self.date_ex_div) + hash(self.amount)

    def __eq__(self, other):
        return (
                self.ticker == other.ticker
                and self.date_ex_div == other.date_ex_div
                and self.amount == other.amount
        )


class StockWatchDividendExtractor:

    BASE_URL = "https://www.stockwatch.pl/async/dividendsview.aspx"

    def fetch_yearly_dividend_info(self, year: int) -> list[DividendInfo]:
        content = self._fetch_raw_year_data(year)
        rows = self._get_soupified_rows(content)
        data = set()
        for row in rows:
            try:
                data.add(self._soup_row_to_dividend_info(row))
            except Exception as e:
                print(f"Error extracting dividend data for year {year}: " + str(e))
        data = list(data)
        data.sort(key=lambda x: x.date_ex_div)
        return data

    def fetch_dividend_data_df(self, years: list[int]) -> pd.DataFrame:
        data = self.fetch_dividend_data(years)
        return pd.DataFrame([div.model_dump() for div in data])

    def fetch_dividend_data_df_csv(self, years: list[int], filename: str) -> None:
        data = self.fetch_dividend_data_df(years)
        data.to_csv(filename, index=False)

    def fetch_dividend_data(self, years: list[int]) -> list[DividendInfo]:
        data = set()
        for year in years:
            try:
                data = data.union(set(self.fetch_yearly_dividend_info(year)))
            except Exception as e:
                print(f"Error extracting dividend data for year {year}: " + str(e))
        data = list(data)
        data.sort(key=lambda x: x.date_ex_div)
        return data

    def _fetch_raw_year_data(self, year: int):
        response = requests.get(self.base_url, params={
            "year": year
        })
        return response.text

    def _get_soupified_rows(self, content: str):
        soup = BeautifulSoup(content, 'html.parser')
        table = soup.find('table', {'id': 'DividendsTab'})
        rows = table.find('tbody').findAll('tr')
        return rows

    # can raise a validation exception if not all required fields are present
    def _soup_row_to_dividend_info(self, row) -> DividendInfo:
        columns = row.findAll('td')

        fields = {
            'ticker': columns[0].find('a').text,
            'amount': columns[3].text.replace(',', '.'),
            'yield_percentage': columns[4].text.replace('%', '').replace(',', '.'),
            'date_payment': columns[5].text,
            'date_ex_div': columns[6].text,
            'date_decision': columns[7].text
        }

        for key, value in fields.items():
            if value == '':
                fields[key] = None

        return DividendInfo(**fields)


if __name__ == '__main__':
    extractor = StockWatchDividendExtractor()
    extractor.fetch_dividend_data_df_csv([2020, 2021, 2022], "test_divs.csv")
