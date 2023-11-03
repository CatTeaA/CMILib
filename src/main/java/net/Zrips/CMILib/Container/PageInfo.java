package net.Zrips.CMILib.Container;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.Locale.LC;
import net.Zrips.CMILib.Messages.CMIMessages;
import net.Zrips.CMILib.RawMessages.RawMessage;
import net.Zrips.CMILib.RawMessages.RawMessageCommand;
import net.Zrips.CMILib.commands.CommandsHandler;

public class PageInfo {

    private int totalEntries = 0;
    private int totalPages = 0;
    private int start = 0;
    private int end = 0;
    private int currentPage = 0;

    private int currentEntry = 0;

    private int perPage = 6;

    private String customPrev = null;
    private String customNext = null;

    private String cmd = null;
    private String pagePref = null;

    public PageInfo(int perPage, int totalEntries, int currentPage) {
        this.perPage = perPage;
        this.totalEntries = totalEntries;
        this.currentPage = currentPage < 1 ? 1 : currentPage;
        calculate();
    }

    public int getPositionForOutput() {
        return currentEntry;
    }

    public int getPositionForOutput(int place) {
        return this.start + place + 1;
    }

    private void calculate() {
        currentEntry = 0;
        this.start = (this.currentPage - 1) * this.perPage;
        this.end = this.start + this.perPage - 1;
        if (this.end + 1 > this.totalEntries)
            this.end = this.totalEntries - 1;
        this.totalPages = (int) Math.ceil((double) this.totalEntries / (double) this.perPage);
    }

    public boolean isInRange(int place) {
        if (place >= start && place <= end)
            return true;
        return false;
    }

    public boolean isEntryOk() {
        currentEntry++;
        return currentEntry - 1 >= start && currentEntry - 1 <= end;
    }

    public boolean isContinue() {
        return !isEntryOk();
    }

    public boolean isContinueNoAdd() {
        return currentEntry - 1 >= start && currentEntry - 1 <= end;
    }

    public boolean isBreak() {
        return currentEntry - 1 > end;
    }

    public boolean isPageOk() {
        return isPageOk(this.currentPage);
    }

    public boolean isPageOk(int page) {
        if (this.totalPages < page)
            return false;
        if (page < 1)
            return false;
        return true;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getNextPageNumber() {
        return this.getCurrentPage() + 1 > this.getTotalPages() ? this.getTotalPages() : this.getCurrentPage() + 1;
    }

    public int getPrevPageNumber() {
        return this.getCurrentPage() - 1 < 1 ? 1 : this.getCurrentPage() - 1;
    }

    public Boolean pageChange(int page) {
        return null;
    }

    public PageInfo setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
        calculate();
        return this;
    }

    @Deprecated
    public void ShowPagination(CommandSender sender, String cmd) {
        ShowPagination(sender, cmd, null);
    }

    @Deprecated
    public void ShowPagination(CMICommandSender sender, String cmd) {
        ShowPagination(sender.getSender(), cmd, null);
    }

    @Deprecated
    public void ShowPagination(CommandSender sender, Object clas, String pagePref) {
        ShowPagination(sender, CommandsHandler.getLabel() + " " + clas.getClass().getSimpleName(), pagePref);
    }

    @Deprecated
    public void ShowPagination(CommandSender sender, String cmd, String pagePref) {
        autoPagination(sender, cmd, pagePref);
    }

    public void autoPagination(CMICommandSender sender, String cmd) {
        autoPagination(sender.getSender(), cmd, null);
    }

    public void autoPagination(CMICommandSender sender, String cmd, String pagePref) {
        autoPagination(sender.getSender(), cmd, pagePref);
    }

    public void autoPagination(CommandSender sender, String cmd) {
        autoPagination(sender, cmd, null);
    }

    public void autoPagination(CommandSender sender) {
        autoPagination(sender, null, null);
    }

    public void autoPagination(CommandSender sender, String cmd, String pagePref) {

        if (cmd == null)
            cmd = this.getPageChangeCommand();

        if (cmd == null)
            return;

        if (getTotalPages() == 1)
            return;
        
        if (pagePref == null)
            pagePref = this.getPageChangeCommandPref();

        String pagePrefix = pagePref == null ? "" : pagePref;

        final int nextPage = getCurrentPage() < getTotalPages() ? getCurrentPage() + 1 : 1;
        final int prevpage = getCurrentPage() > 1 ? getCurrentPage() - 1 : getTotalPages();

        if (!(sender instanceof Player)) {
            CMIMessages.sendMessage(sender, LC.info_nextPageConsole, "[command]", (cmd.replace("/", "") + " " + pagePrefix + nextPage));
            return;
        }

        RawMessage rm = new RawMessage();
        RawMessageCommand rmcb = new RawMessageCommand() {
            @Override
            public void run(CommandSender sender) {
                if (pageChange(prevpage) != null)
                    return;
                String originalCmd = getOriginalCommand();
                if (originalCmd != null)
                    Bukkit.dispatchCommand(sender, originalCmd);
            }
        };

        rmcb.setOriginalCommand((cmd.replace("/", "") + " " + pagePrefix + prevpage));

        String prevText = this.getCustomPrev() == null ? (getCurrentPage() > 1 ? LC.info_prevPage : LC.info_prevPageOff).getLocale() : (getCurrentPage() > 1 ? LC.info_prevCustomPage
            : LC.info_prevCustomPageOff).getLocale("[value]", this.getCustomPrev());

        rm.addText(prevText)
            .addHover(getCurrentPage() > 1 ? LC.info_prevPageHover.getLocale() : LC.info_lastPageHover.getLocale())
            .addCommand(rmcb.getCommand());

        rm.addText(LC.info_pageCount.getLocale("[current]", getCurrentPage(), "[total]", getTotalPages())).addHover(LC.info_pageCountHover.getLocale("[totalEntries]", getTotalEntries()));

        RawMessageCommand rmcf = new RawMessageCommand() {
            @Override
            public void run(CommandSender sender) {
                if (pageChange(nextPage) != null)
                    return;
                String originalCmd = getOriginalCommand();
                if (originalCmd != null)
                    Bukkit.dispatchCommand(sender, originalCmd);
            }
        };
        rmcf.setOriginalCommand((cmd.replace("/", "") + " " + pagePrefix + nextPage));

        String nextText = this.getCustomNext() == null ? (getTotalPages() > getCurrentPage() ? LC.info_nextPage : LC.info_nextPageOff).getLocale() : (getTotalPages() > getCurrentPage()
            ? LC.info_nextCustomPage : LC.info_nextCustomPageOff).getLocale("[value]", this.getCustomNext());

        rm.addText(nextText)
            .addHover(getTotalPages() > getCurrentPage() ? LC.info_nextPageHover.getLocale() : LC.info_firstPageHover.getLocale())
            .addCommand(rmcf.getCommand());
        if (getTotalPages() != 0)
            rm.show(sender);
    }

    public String getCustomPrev() {
        return customPrev;
    }

    public void setCustomPrev(String CustomPrev) {
        this.customPrev = CustomPrev;
    }

    public String getCustomNext() {
        return customNext;
    }

    public void setCustomNext(String CustomNext) {
        this.customNext = CustomNext;
    }

    public String getPageChangeCommand() {
        return cmd;
    }

    public void setPageChangeCommand(String cmd) {
        this.cmd = cmd;
    }

    public String getPageChangeCommandPref() {
        return pagePref;
    }

    public void setPageChangeCommandPref(String pagePref) {
        this.pagePref = pagePref;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
        calculate();
    }
}
