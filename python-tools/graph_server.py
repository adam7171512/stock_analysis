import time
from flask import Flask, request, jsonify, url_for
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.colors import LinearSegmentedColormap
import matplotlib


"""
Those are quickly prototyped endpoints for generating heatmaps and plots for visual testing purposes.
"""

matplotlib.use('Agg')
app = Flask(__name__)
colors = ["blue", "red"]
cmap = LinearSegmentedColormap.from_list("custom", colors, N=256)


@app.route('/heatmap', methods=['POST'])
def heatmap():
    data = request.get_json()
    rows = data['rows']
    columns = data['cols']
    values = data['data']
    title = data.get('title', '')  # Fetching title from the JSON, default to empty string if not present
    rowsLabel = data.get('rowsLabel', '')  # Fetching rowsLabel from the JSON
    colsLabel = data.get('colsLabel', '')  # Fetching colsLabel from the JSON

    values = np.array(values)

    plt.figure(figsize=(20, 14))

    # Use very small font before creating the heatmap
    plt.rcParams.update({'font.size': 6})

    ax = sns.heatmap(values, annot=True, cmap='RdBu_r', yticklabels=rows, xticklabels=columns)

    plt.title(title)  # Setting the heatmap title
    plt.ylabel(rowsLabel)  # Setting the y-axis label
    plt.xlabel(colsLabel)  # Setting the x-axis label
    plt.tight_layout()

    # Generating the timestamped filename
    filename = f"heatmap_{int(time.time())}.png"
    file_path = f"static/{filename}"

    # Saving the image with the generated filename
    plt.savefig(file_path)
    print("lol")
    # Construct the URL
    heatmap_url = url_for('static', filename=filename, _external=True)

    return jsonify({'result': 'success', 'heatmap_url': heatmap_url})


@app.route('/plot', methods=['POST'])
def plot_endpoint():
    data = request.json
    average_returns = {int(k): float(v) for k, v in data['average_returns'].items()}
    sorted_days = sorted(average_returns.keys())
    sorted_returns = [average_returns[day] for day in sorted_days]

    # Adjusting the cumulative returns calculation for compounding
    cum_returns = [100]
    for ret in sorted_returns:
        cum_returns.append(cum_returns[-1] * (1 + ret))

    fig, ax1 = plt.subplots(figsize=(20, 14))

    # Bar plot for average returns on primary y-axis
    ax1.bar(sorted_days, sorted_returns, label="Average Returns", alpha=0.7)
    ax1.set_xlabel("Days relative to ex-dividend")
    ax1.set_ylabel("Returns")
    ax1.axhline(0, color='black', linewidth=0.5)
    ax1.axvline(0, color='black', linewidth=0.5)
    ax1.grid(True, which='both', linestyle='--', linewidth=0.5)

    # Line plot for cumulative returns on secondary y-axis
    ax2 = ax1.twinx()
    ax2.plot(sorted_days, cum_returns[1:], color="red", marker="o", label="Cumulative Returns")
    ax2.set_ylabel('Cumulative Returns starting from 100')

    fig.tight_layout()
    fig.legend(loc="upper left", bbox_to_anchor=(0.1, 0.92))
    fig.suptitle("Returns near Ex-Dividend Date")

    # Save the plot
    filename = f"plot_{int(time.time())}.png"
    file_path = f"static/{filename}"
    plt.savefig(file_path)

    # Return the URL of the saved image
    plot_url = url_for('static', filename=filename, _external=True)
    return jsonify({'result': 'success', 'plot_url': plot_url})


from datetime import datetime


@app.route('/strategy', methods=['POST'])
def strategy_analysis():
    data = request.json

    # Extract main values from data
    analysis_date = data.get("analysisDate")
    starting_capital = float(data.get("startingCapital", 0))
    total_profit = float(data.get("totalProfit", 0))
    start_date = data.get("startDate")
    end_date = data.get("endDate")
    roi_percentage = data.get("roiPercentage")
    yearly_adjusted_roi_percentage = data.get("yearlyAdjustedRoiPercentage")
    average_cash_balance = float(data.get("averageCashBalance", 0))

    # Extract snapshots
    snapshots = data.get("snapshots", [])
    snapshot_dates = [datetime.strptime(item["date"], "%Y-%m-%d") for item in snapshots]
    snapshot_values = [float(item["value"]) for item in snapshots]
    cash_balances = [float(item["cash"]) for item in snapshots]
    num_holdings = [len(item["holdings"]) for item in snapshots]

    fig, ax1 = plt.subplots(figsize=(20, 14))

    # Plot Investment values and Cash balances
    ax1.plot(snapshot_dates, snapshot_values, color="blue", marker="o", label="Investment Value over Time")
    ax1.plot(snapshot_dates, cash_balances, color="green", marker="x", label="Cash Balance over Time")
    ax1.set_xlabel("Date")
    ax1.set_ylabel("Value", color="blue")
    ax1.grid(True, which='both', linestyle='--', linewidth=0.5)
    ax1.legend(loc="upper left")

    # Create another y-axis to plot number of holdings
    ax2 = ax1.twinx()
    ax2.plot(snapshot_dates, num_holdings, color="red", linestyle="--", marker="s", label="Number of Holdings")
    ax2.set_ylabel("Number of Holdings", color="red")
    ax2.legend(loc="upper right")

    fig.suptitle(
        f"Investment Value Analysis\nStarting Number of Holdings: {num_holdings[0] if num_holdings else 'N/A'}\n"
        f"Starting Balance: ${starting_capital}\nEnding Balance: ${snapshot_values[-1] if snapshot_values else 'N/A'}\n"
        f"Profit: ${total_profit}\nROI: {roi_percentage}%\nYearly-Adjusted ROI: {yearly_adjusted_roi_percentage}%\n"
        f"Average Free Cash Ratio: {average_cash_balance / starting_capital if starting_capital else 'N/A'}")

    # Save the plot
    filename = f"strategy_analysis_{int(time.time())}.png"
    file_path = f"static/{filename}"
    plt.savefig(file_path)

    # Return the URL of the saved image
    plot_url = url_for('static', filename=filename, _external=True)
    return jsonify({'result': 'success', 'plot_url': plot_url})


@app.route('/strategycomparison', methods=['POST'])
def strategy_comparison():
    data_list = request.json  # assuming this is now a list of JSON objects for different strategies

    fig, ax1 = plt.subplots(figsize=(20, 14))

    color_palette = ['blue', 'green', 'red', 'cyan', 'magenta', 'yellow', 'black']
    color_index = 0

    for data in data_list:
        snapshot_data = data['snapshots']
        snapshot_dates = [datetime.strptime(snapshot['date'], "%Y-%m-%d") for snapshot in snapshot_data]
        snapshot_values = [float(snapshot['value']) for snapshot in snapshot_data]
        cash_balances = [float(snapshot['cash']) for snapshot in snapshot_data]
        num_holdings = [len(snapshot['holdings']) for snapshot in snapshot_data]

        color = color_palette[color_index]

        # Plot Investment values
        ax1.plot(snapshot_dates, snapshot_values, color=color, marker="o",
                 label=f"Investment Value of Strategy {color_index + 1}")

        # Plot Cash balances
        ax1.plot(snapshot_dates, cash_balances, color=color, linestyle='--', marker="x",
                 label=f"Cash Balance of Strategy {color_index + 1}")

        # Create another y-axis to plot number of holdings
        ax2 = ax1.twinx()
        ax2.plot(snapshot_dates, num_holdings, color=color, linestyle=':', marker="s",
                 label=f"Number of Holdings of Strategy {color_index + 1}")

        color_index = (color_index + 1) % len(color_palette)  # cycle through the color palette

    ax1.set_xlabel("Date")
    ax1.set_ylabel("Value", color="blue")
    ax1.grid(True, which='both', linestyle='--', linewidth=0.5)
    ax1.legend(loc="upper left")

    ax2.set_ylabel("Number of Holdings", color="red")
    ax2.legend(loc="upper right")

    fig.suptitle("Strategy Comparison Analysis")

    # Save the plot
    filename = f"strategy_comparison_{int(time.time())}.png"
    file_path = f"static/{filename}"
    plt.savefig(file_path)

    # Return the URL of the saved image
    plot_url = url_for('static', filename=filename, _external=True)
    return jsonify({'result': 'success', 'plot_url': plot_url})


if __name__ == "__main__":
    app.run(debug=True)
