import datetime
import aiohttp
import asyncio
import logging

logging.basicConfig(level=logging.INFO)


class GptDataFetch:
    """
    This class fetches the daily xlsx files with stock data from the GPW website.
    It uses asyncio to download the files in parallel.
    """

    def __init__(self, semaphore_size: int = 5, directory: str = "data"):
        self.semaphore = asyncio.Semaphore(semaphore_size)
        self.directory = directory

    @staticmethod
    def _get_url(date: datetime.datetime):
        return f"https://www.gpw.pl/archiwum-notowan?fetch=1&type=10&instrument=&date={date.strftime('%d-%m-%Y')}"

    async def download_stock_data_xlsx(self, date: datetime.datetime):
        url = self._get_url(date)
        logging.info(f"Attempting to download {url}")

        async with aiohttp.ClientSession() as session:
            try:
                headers = {
                    'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) '
                                  'AppleWebKit/537.36 (KHTML, like Gecko) '
                                  'Chrome/51.0.2704.103 Safari/537.36',
                    'Accept-Encoding': 'gzip, deflate',
                    'Accept': '*/*',
                    'Connection': 'keep-alive',
                }

                async with session.get(url, headers=headers) as resp:
                    if resp.status == 200:
                        file_path = f"{self.directory + '/' + date.strftime('%Y-%m-%d')}.xls"
                        try:
                            with open(file_path, 'wb') as f:
                                f.write(await resp.read())
                            logging.info(f"Successfully downloaded {url}")
                        except IOError as e:
                            logging.error(f"Error writing to file {file_path}. Error: {e}")
                    elif resp.status == 404:
                        logging.error(f"URL not found: {url}")
                    else:
                        logging.error(f"Failed to download {url} with status code: {resp.status}")
            except aiohttp.ClientConnectorError:
                logging.error(f"Failed to connect to {url}")
            except aiohttp.ClientError as e:
                logging.error(f"Client error while trying to connect to {url}. Error: {e}")

    async def bounded_download(self, date):
        async with self.semaphore:
            await self.download_stock_data_xlsx(date)

    async def fetch_data(self, date_from: datetime.date, date_to: datetime.date):
        dates = self._get_dates_from_range(date_from, date_to)
        tasks = []

        for date in dates:
            print(f"Adding task for {date}")
            tasks.append(asyncio.create_task(self.bounded_download(date)))

        await asyncio.gather(*tasks)

    def _get_dates_from_range(self, date_from: datetime.date, date_to: datetime.date):
        dates = []
        while date_from <= date_to:
            if date_from.isoweekday() <= 5:
                # Skip weekends
                dates.append(date_from)
            date_from += datetime.timedelta(days=1)
        return dates


if __name__ == "__main__":
    fetcher = GptDataFetch()
    asyncio.run(fetcher.fetch_data(datetime.date(2023, 1, 1), datetime.date(2023, 1, 31)))
