from django.shortcuts import render
import json
from django.http import HttpResponse
from django.middleware import csrf
from django.views.decorators.csrf import csrf_exempt
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import threading
import os

# Create your views here.
def draw_map(x_cord, y_cord, tick_label):
    fig = plt.figure()
    # ax = fig.add_axes([0.1, 0.1, 0.8, 0.8])
    # ax.plot(x_cord, y_cord)
    plt.plot(x_cord, y_cord, color = "green", linestyle = "dashed", linewidth = 2, marker = "o", markerfacecolor = "blue", markersize = 8)
    plt.xlabel("day number from stating date ")
    plt.ylabel("expenditure")
    plt.title("Expense Plot")
    #color = "green", linestyle = "dashed", linewidth = 3,
    #marker = "o", markerfacecolor = "blue", markersize = 12"""
    # ax.set_xlabel("day number from stating date ")
    # ax.set_ylabel("expenditure")
    # ax.set_title("Expense Plot")
    # ax.set_xticklabels(tick_label)
    # ax.set_xticks([i*4  for i in x_cord])
    plt.savefig("mapserverapp/static/media/mapserverapp/expense_plot.png")
    plt.close()
    global image_drawn
    image_drawn = True
    #image = drawMap()

@csrf_exempt
def home(request):
    if request.method == "POST":
        print("Got http POST request")
        if request.body != None:
            print("request body is not None")
            received_json_data = json.loads(request.body)
            y_cord = received_json_data["val_daily_expen"]
            x_cord = [ i for i in range(len(y_cord))]
            tick_label = received_json_data["key_daily_expen"]
            print(x_cord, y_cord, tick_label, sep="\n")
            draw_map(x_cord, y_cord, tick_label)
            try:
                with open('mapserverapp/static/media/mapserverapp/expense_plot.png', 'rb') as fh:
                    response = HttpResponse(fh.read(), content_type="image/png")
                    response['Content-Disposition'] = 'inline; filename=' + 'expense_plot.png'
                    #os.remove('expense_plot.png')
                    return response
            except Exception as e:
                print(e)
                return HttpResponse("Failed to download")
        else:
            print("request body is None")
    else:
        return HttpResponse("Request not supported");